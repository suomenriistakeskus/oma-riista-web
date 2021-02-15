package fi.riista.feature.dashboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.QMooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class DashboardMooselikeEndOfHuntingExcelFeature {

    private static final QMooseHuntingSummary MOOSE_HUNTING_SUMMARY = QMooseHuntingSummary.mooseHuntingSummary;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QHarvestPermitSpeciesAmount PERMIT_SPA =
            QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QHuntingClub CLUB = QHuntingClub.huntingClub;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static final QOrganisation RKA = new QOrganisation("rka");

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private ClubHuntingSummaryBasicInfoService clubHuntingSummaryBasicInfoService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public AbstractXlsxView exportMooselikeHuntingSummaries(final int speciesCode,
                                                            final int huntingYear,
                                                            final Locale locale) {
        Preconditions.checkArgument(GameSpecies.isMooseOrDeerRequiringPermitForHunting(speciesCode));

        if (GameSpecies.isMoose(speciesCode)) {
            return exportMooseReports(huntingYear, locale);
        } else {
            return exportDeerReports(speciesCode, huntingYear, locale);
        }
    }

    private DashboardDeerEndOfHuntingExcelView exportDeerReports(final int speciesCode, final int huntingYear,
                                                                 final Locale locale) {
        final Map<Long, String> permitIdToNumber = jpqlQueryFactory
                .from(PERMIT_SPA)
                .innerJoin(PERMIT_SPA.harvestPermit, PERMIT)
                .innerJoin(PERMIT_SPA.gameSpecies, SPECIES)
                .where(PERMIT.permitYear.eq(huntingYear))
                .where(SPECIES.officialCode.eq(speciesCode))
                .where(PERMIT.permitTypeCode.eq(PermitTypeCode.MOOSELIKE))
                .transform(groupBy(PERMIT.id).as(PERMIT.permitNumber));

        final ClubHuntingSummaryBasicInfoByPermitAndClub huntingSummaries =
                clubHuntingSummaryBasicInfoService.getHuntingSummaries(permitIdToNumber.keySet(), speciesCode);
        final List<ClubHuntingSummaryBasicInfoDTO> finishedSummaries =
                huntingSummaries.streamSummaries()
                        .filter(ClubHuntingSummaryBasicInfoDTO::isHuntingFinished)
                        .collect(toList());
        final List<Long> clubIds = F.mapNonNullsToList(finishedSummaries, ClubHuntingSummaryBasicInfoDTO::getClubId);
        final List<Long> permitIds =
                F.mapNonNullsToList(finishedSummaries, ClubHuntingSummaryBasicInfoDTO::getPermitId);

        final Map<Long, HuntingClubInfoDTO> clubInfoMapping = clubIds.isEmpty()
                ? ImmutableMap.of()
                : jpqlQueryFactory
                        .from(CLUB)
                        .where(CLUB.id.in(clubIds))
                        .transform(GroupBy.groupBy(CLUB.id).as(CLUB.nameLocalisation(), CLUB.geoLocation))
                        .entrySet().stream()
                        .collect(toMap(
                                e -> e.getKey(),
                                e -> {
                                    final Group group = e.getValue();
                                    return HuntingClubInfoDTO.from(
                                            group.getOne(CLUB.nameLocalisation()),
                                            group.getOne(CLUB.geoLocation));
                                }
                        ));


        final Map<Long, PermitInfoDTO> permitInfoMapping = getPermitInfoMapping(permitIds);


        final Function<ClubHuntingSummaryBasicInfoDTO, PermitInfoDTO> keyExtractor =
                summary -> permitInfoMapping.get(summary.getPermitId());

        final List<ClubHuntingSummaryBasicInfoDTO> sortedSummaries = sort(finishedSummaries, keyExtractor, locale);

        return new DashboardDeerEndOfHuntingExcelView(
                sortedSummaries, permitIdToNumber, clubInfoMapping, permitInfoMapping,
                new EnumLocaliser(messageSource, locale));

    }

    private DashboardMooseEndOfHuntingExcelView exportMooseReports(final int huntingYear, final Locale locale) {
        final List<DashboardHuntingSummaryDTO> dtoList = jpqlQueryFactory
                .select(PERMIT.id,
                        CLUB.nameLocalisation(),
                        CLUB.geoLocation,
                        MOOSE_HUNTING_SUMMARY,
                        PERMIT.permitNumber)
                .from(MOOSE_HUNTING_SUMMARY)
                .innerJoin(MOOSE_HUNTING_SUMMARY.harvestPermit, PERMIT)
                .innerJoin(MOOSE_HUNTING_SUMMARY.club, CLUB)
                .where(PERMIT.permitYear.eq(huntingYear),
                        MOOSE_HUNTING_SUMMARY.huntingFinished.isTrue())
                .fetch()
                .stream()
                .map(t -> DashboardHuntingSummaryDTO.from(
                        t.get(MOOSE_HUNTING_SUMMARY),
                        t.get(PERMIT.permitNumber),
                        t.get(PERMIT.id),
                        t.get(CLUB.nameLocalisation()),
                        t.get(CLUB.geoLocation)))
                .collect(toList());

        final Set<Long> permitIds = F.getUniqueIds(dtoList);
        final Map<Long, PermitInfoDTO> permitInfoMapping = getPermitInfoMapping(permitIds);

        final Function<DashboardHuntingSummaryDTO, PermitInfoDTO> keyExtractor =
                summary -> permitInfoMapping.get(summary.getPermitId());

        final List<DashboardHuntingSummaryDTO> sortedSummaries = sort(dtoList, keyExtractor, locale);

        return new DashboardMooseEndOfHuntingExcelView(
                sortedSummaries, permitInfoMapping, new EnumLocaliser(messageSource, locale));
    }

    private Map<Long, PermitInfoDTO> getPermitInfoMapping(final Collection<Long> permitIds) {
        if (permitIds.isEmpty()) {
            return ImmutableMap.of();
        }

        return jpqlQueryFactory
                .from(PERMIT)
                .innerJoin(PERMIT.rhy, RHY)
                .innerJoin(RHY.parentOrganisation, RKA)
                .where(PERMIT.id.in(permitIds))
                .transform(groupBy(PERMIT.id).as(
                        PERMIT.id,
                        RHY.nameLocalisation(),
                        RKA.nameLocalisation()))
                .entrySet()
                .stream()
                .collect(toMap(
                        e -> e.getKey(),
                        e -> {
                            final Group group = e.getValue();
                            return PermitInfoDTO.create(
                                    group.getOne(PERMIT.id),
                                    group.getOne(RHY.nameLocalisation()),
                                    group.getOne(RKA.nameLocalisation()));
                        }
                ));
    }

    private static <DTO> List<DTO> sort(final List<DTO> list, Function<DTO, PermitInfoDTO> keyExtractor, Locale locale) {
        final Comparator<PermitInfoDTO> clubParentComparator =
                Comparator.comparing((PermitInfoDTO dto) -> dto.getRka().getTranslation(locale))
                        .thenComparing((PermitInfoDTO dto) -> dto.getRhy().getTranslation(locale))
                        .thenComparing(PermitInfoDTO::getPermitId);

        return list.stream()
                .sorted(Comparator.comparing(keyExtractor, clubParentComparator))
                .collect(toList());

    }
}
