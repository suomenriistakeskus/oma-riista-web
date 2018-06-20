package fi.riista.feature.harvestpermit.statistics;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gis.hta.QGISHirvitalousalue;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.endofhunting.QMooseHarvestReport;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryService;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.search.RhySearchParamsFeature;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toList;

@Service
public class MoosePermitStatisticsService {

    @Resource
    private ClubHuntingSummaryService clubHuntingSummaryService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MoosePermitStatisticsDTO> calculateByPartner(final Locale locale,
                                                             final int speciesCode,
                                                             final int huntingYear,
                                                             final List<HarvestPermit> permits) {

        final Map<Long, Double> permitAmounts = findPermitAmounts(permits, speciesCode, huntingYear);
        final Map<Long, Boolean> permitIdToPermitLocked =
                getPermitIdToMooseHarvestReportExistsMapping(permits, speciesCode, huntingYear);

        return permits.stream()
                .flatMap(permit -> {
                    final Map<Long, BasicClubHuntingSummaryDTO> partnerSummaries = F.index(
                            clubHuntingSummaryService.getHuntingSummariesForModeration(permit, speciesCode),
                            BasicClubHuntingSummaryDTO::getClubId);

                    return permit.getPermitPartners().stream()
                            .map(partner -> {
                                final List<BasicClubHuntingSummaryDTO> summaries = singletonList(partnerSummaries.get(partner.getId()));
                                return createStatisticsDto(
                                        permitAmounts, permitIdToPermitLocked, permit, partner, summaries, false);
                            });
                })
                .sorted(getSort(locale))
                .collect(toList());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MoosePermitStatisticsDTO> calculateByHolder(final Locale locale,
                                                            final int speciesCode,
                                                            final int huntingYear,
                                                            final RhySearchParamsFeature.RhySearchOrgType orgType,
                                                            final String orgCode) {

        final List<HarvestPermit> permits = findPermits(orgType, orgCode, speciesCode, huntingYear);
        final List<MoosePermitStatisticsDTO> stats = calculateByHolder(locale, speciesCode, huntingYear, permits);
        return F.concat(singletonList(createTotal(stats)), stats);
    }

    private List<HarvestPermit> findPermits(final RhySearchParamsFeature.RhySearchOrgType orgType,
                                            final String orgCode,
                                            final int speciesCode,
                                            final int huntingYear) {

        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies gameSpecies = QGameSpecies.gameSpecies;
        final QHuntingClub permitHolder = QHuntingClub.huntingClub;

        final BooleanExpression orgPredicate = findOrganisationPredicate(orgType, orgCode, permit);
        return jpqlQueryFactory.selectFrom(permit)
                .join(permit.permitHolder, permitHolder).fetchJoin()
                .join(permit.speciesAmounts, speciesAmount)
                .join(speciesAmount.gameSpecies, gameSpecies)
                .where(orgPredicate,
                        permit.permitTypeCode.eq(HarvestPermit.MOOSELIKE_PERMIT_TYPE),
                        gameSpecies.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .fetch();
    }

    private List<MoosePermitStatisticsDTO> calculateByHolder(final Locale locale,
                                                             final int speciesCode,
                                                             final int huntingYear,
                                                             final List<HarvestPermit> permits) {

        final Map<Long, Double> permitAmounts = findPermitAmounts(permits, speciesCode, huntingYear);
        final Map<Long, Boolean> permitIdToPermitLocked =
                getPermitIdToMooseHarvestReportExistsMapping(permits, speciesCode, huntingYear);

        return permits.stream()
                .map(permit -> {
                    final HuntingClub permitHolder = permit.getPermitHolder();
                    final List<BasicClubHuntingSummaryDTO> basicHuntingSummaries =
                            clubHuntingSummaryService.getHuntingSummariesForModeration(permit, speciesCode);
                    return createStatisticsDto(permitAmounts, permitIdToPermitLocked, permit, permitHolder,
                            basicHuntingSummaries, true);
                })
                .sorted(getSort(locale))
                .collect(toList());
    }

    private static MoosePermitStatisticsDTO createStatisticsDto(final Map<Long, Double> permitAmounts,
                                                                final Map<Long, Boolean> permitIdToPermitLocked,
                                                                final HarvestPermit permit,
                                                                final HuntingClub permitHolder,
                                                                final List<BasicClubHuntingSummaryDTO> basicHuntingSummaries,
                                                                final boolean limitToPermitAreaSize) {

        final double permitAmount = permitAmounts.getOrDefault(permit.getId(), 0.0);
        final LocalisedString permitHolderName = permitHolder.getNameLocalisation();
        final String permitHolderOfficialCode = permitHolder.getOfficialCode();
        final boolean locked = permitIdToPermitLocked.get(permit.getId());
        final MoosePermitStatisticsCount harvestCount = MoosePermitStatisticsCount.create(
                permit.getPermitAreaSize(), limitToPermitAreaSize, basicHuntingSummaries);

        return new MoosePermitStatisticsDTO()
                .withPermitId(permit.getId())
                .withPermitNumber(permit.getPermitNumber())
                .withPermitHolderName(permitHolderName)
                .withPermitHolderOfficialCode(permitHolderOfficialCode)
                .withHuntingFinished(locked)
                .withPermitAmount(permitAmount)
                .withHarvestCount(harvestCount);
    }

    private static Comparator<MoosePermitStatisticsDTO> getSort(final Locale locale) {
        return Comparator.comparing(MoosePermitStatisticsDTO::getPermitNumber)
                .thenComparing(comparing(a -> a.getPermitHolderLocalisedString().getAnyTranslation(locale)));
    }

    private static BooleanExpression findOrganisationPredicate(final RhySearchParamsFeature.RhySearchOrgType orgType,
                                                               final String orgCode,
                                                               final QHarvestPermit permit) {

        final QRiistanhoitoyhdistys rhy = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        switch (orgType) {
            case RHY:
                return permit.rhy.id.eq(JPAExpressions.select(rhy.id).from(rhy).where(rhy.officialCode.eq(orgCode)));
            case RKA:
                final QRiistakeskuksenAlue rka = QRiistakeskuksenAlue.riistakeskuksenAlue;
                return permit.rhy.id.in(JPAExpressions.select(rhy.id)
                        .from(rka)
                        .join(rka.subOrganisations, rhy._super)
                        .where(rka.officialCode.eq(orgCode)));
            case HTA:
                final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
                return permit.mooseArea.id.eq(JPAExpressions.select(hta.id).from(hta).where(hta.number.eq(orgCode)));
            default:
                // Fall-through to throwing exception.
        }
        throw new IllegalStateException("Unknown orgType: " + orgType);
    }

    private Map<Long, Double> findPermitAmounts(final List<HarvestPermit> permits,
                                                final int speciesCode,
                                                final int huntingYear) {
        final Map<Long, Float> amounts = findAmountsByPermitId(permits, speciesCode, huntingYear);
        final Map<Long, Float> amendmentAmounts = findAmendmentAmountsByOriginalPermitId(permits, speciesCode, huntingYear);

        return Stream.concat(amounts.entrySet().stream(), amendmentAmounts.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey, summingDouble(Map.Entry::getValue)));
    }

    private Map<Long, Float> findAmountsByPermitId(final List<HarvestPermit> permits,
                                                   final int speciesCode,
                                                   final int huntingYear) {

        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        return jpqlQueryFactory.select(speciesAmount.harvestPermit.id, speciesAmount.amount)
                .from(speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .where(speciesAmount.harvestPermit.in(permits),
                        species.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .transform(groupBy(speciesAmount.harvestPermit.id).as(speciesAmount.amount));
    }

    private Map<Long, Float> findAmendmentAmountsByOriginalPermitId(final List<HarvestPermit> originalPermits,
                                                                    final int speciesCode,
                                                                    final int huntingYear) {

        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;
        final QHarvestPermit amendmentPermit = new QHarvestPermit("amendmentPermit");

        final NumberPath<Long> originalPermitId = amendmentPermit.originalPermit.id;
        final NumberExpression<Float> sumOfAmendmentPermitAmounts = speciesAmount.amount.sum();

        return jpqlQueryFactory.select(originalPermitId, sumOfAmendmentPermitAmounts)
                .from(speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .join(speciesAmount.harvestPermit, amendmentPermit)
                .where(amendmentPermit.originalPermit.in(originalPermits),
                        species.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .groupBy(originalPermitId)
                .transform(groupBy(originalPermitId).as(sumOfAmendmentPermitAmounts));
    }

    private Map<Long, Boolean> getPermitIdToMooseHarvestReportExistsMapping(final List<HarvestPermit> permits,
                                                                            final int speciesCode,
                                                                            final int huntingYear) {
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QMooseHarvestReport report = QMooseHarvestReport.mooseHarvestReport;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        final JPQLQuery<Object> mooseHarvestReportQuery = jpqlQueryFactory.select(Expressions.nullExpression())
                .from(report)
                .where(report.speciesAmount.eq(speciesAmount));

        final Expression<Boolean> mooseHarvestReportExists = new CaseBuilder()
                .when(mooseHarvestReportQuery.exists()).then(Expressions.constant(true))
                .otherwise(Expressions.constant(false));

        return jpqlQueryFactory.select(speciesAmount.harvestPermit.id, mooseHarvestReportExists)
                .from(speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .where(speciesAmount.harvestPermit.in(permits),
                        species.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .transform(groupBy(speciesAmount.harvestPermit.id).as(mooseHarvestReportExists));
    }

    private static MoosePermitStatisticsDTO createTotal(final List<MoosePermitStatisticsDTO> stats) {

        double permitAmount = stats.stream().mapToDouble(MoosePermitStatisticsDTO::getPermitAmount).sum();

        final int totalPermitAreaSize = stats.stream().mapToInt(s -> s.getHarvestCount().getTotalAreaSize()).sum();
        final int effectiveAreaSize = stats.stream().mapToInt(s -> s.getHarvestCount().getEffectiveAreaSize()).sum();
        final int remainingPopulationInTotalArea = stats.stream().mapToInt(s -> s.getHarvestCount().getRemainingPopulationInTotalArea()).sum();
        final int remainingPopulationInEffectiveArea = stats.stream().mapToInt(s -> s.getHarvestCount().getRemainingPopulationInEffectiveArea()).sum();
        final int adultMales = stats.stream().mapToInt(s -> s.getHarvestCount().getAdultMales()).sum();
        final int adultFemales = stats.stream().mapToInt(s -> s.getHarvestCount().getAdultFemales()).sum();
        final int youngMales = stats.stream().mapToInt(s -> s.getHarvestCount().getYoungMales()).sum();
        final int youngFemales = stats.stream().mapToInt(s -> s.getHarvestCount().getYoungFemales()).sum();
        final int adultsNonEdible = stats.stream().mapToInt(s -> s.getHarvestCount().getAdultsNonEdible()).sum();
        final int youngNonEdible = stats.stream().mapToInt(s -> s.getHarvestCount().getYoungNonEdible()).sum();

        final MoosePermitStatisticsCount harvestCount = new MoosePermitStatisticsCount(
                totalPermitAreaSize,
                effectiveAreaSize,
                remainingPopulationInTotalArea,
                remainingPopulationInEffectiveArea,
                adultMales, adultFemales, youngMales, youngFemales, adultsNonEdible, youngNonEdible);
        return new MoosePermitStatisticsDTO()
                .withPermitAmount(permitAmount)
                .withHarvestCount(harvestCount);
    }
}
