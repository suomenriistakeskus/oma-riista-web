package fi.riista.integration.lupahallinta;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.integration.lupahallinta.club.LHMooselikeHarvestsCSVRow;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class MooselikeHarvestExportToLupahallintaFeature {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestCountService harvestCountService;

    @Resource
    private ClubHuntingSummaryBasicInfoService clubHuntingSummaryBasicInfoService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_MOOSELIKE_HARVESTS')")
    public List<LHMooselikeHarvestsCSVRow> exportToCSV(final int huntingYear) {
        return GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.stream()
                .flatMap(speciesCode -> getRows(huntingYear, speciesCode))
                .sorted(comparing(LHMooselikeHarvestsCSVRow::getPermitNumber)
                        .thenComparing(LHMooselikeHarvestsCSVRow::getCustomerNumber))
                .collect(toList());
    }

    @Nonnull
    private Stream<LHMooselikeHarvestsCSVRow> getRows(final int huntingYear, final int speciesCode) {
        final List<HarvestPermit> permits = harvestPermitRepository.findMooselikePermits(huntingYear, speciesCode);
        final Set<Long> permitIds = F.getUniqueIds(permits);

        final HarvestCountByPermitAndClub harvestCounts =
                harvestCountService.countHarvestsGroupingByPermitAndClubId(permitIds, speciesCode);

        final ClubHuntingSummaryBasicInfoByPermitAndClub huntingSummaries =
                clubHuntingSummaryBasicInfoService.getHuntingSummaries(permitIds, speciesCode);

        return permits.stream()
                .flatMap(permit -> {
                    return permit.getPermitPartners()
                            .stream()
                            .map(partner -> createRow(speciesCode, permit, partner, harvestCounts, huntingSummaries));
                });
    }

    private static LHMooselikeHarvestsCSVRow createRow(final int speciesCode,
                                                       final HarvestPermit permit,
                                                       final HuntingClub partner,
                                                       final HarvestCountByPermitAndClub harvestCounts,
                                                       final ClubHuntingSummaryBasicInfoByPermitAndClub huntingSummaries) {

        final int permitAreaSize = requireNonNull(permit.getPermitAreaSize());
        final HasHarvestCountsForPermit harvestCount = requireNonNull(harvestCounts.findCount(permit, partner));
        final ClubHuntingSummaryBasicInfoDTO huntingSummary = requireNonNull(huntingSummaries.findSummary(permit, partner));

        return new LHMooselikeHarvestsCSVRow(permit.getPermitNumber(), partner.getOfficialCode(), speciesCode,
                harvestCount, huntingSummary, permitAreaSize);
    }
}
