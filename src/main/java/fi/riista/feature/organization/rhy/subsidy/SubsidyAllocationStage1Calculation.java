package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.roundTotalSubsidyShare;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

// In stage 1, total subsidy amount is allocated to RHYs based on annual
// statistics of previous year.
public final class SubsidyAllocationStage1Calculation {

    public static List<RhySubsidyStage1DTO> calculateStatisticsBasedSubsidyAllocation(
            final int subsidyYear,
            @Nonnull final List<AnnualStatisticsExportDTO> allRhyStatistics,
            @Nonnull final List<SubsidyAllocatedToCriterionDTO> criteriaAllocations) {

        requireNonNull(criteriaAllocations);

        final Map<SubsidyAllocationCriterion, BigDecimal> unitAmountIndex =
                criteriaAllocations.stream().collect(toMap(dto -> dto.getCriterion(), dto -> dto.getUnitAmount()));

        final StatisticsBasedSubsidyShareCalculator shareCalculator =
                new StatisticsBasedSubsidyShareCalculator(unitAmountIndex);

        return F.mapNonNullsToList(allRhyStatistics, statistics ->
                createAllocation(subsidyYear, statistics, shareCalculator));
    }

    private static RhySubsidyStage1DTO createAllocation(final int subsidyYear,
                                                        final AnnualStatisticsExportDTO statistics,
                                                        final StatisticsBasedSubsidyShareCalculator shareCalculator) {

        final StatisticsBasedSubsidyShareDTO calculatedShares =
                shareCalculator.calculateSubsidyShare(subsidyYear, statistics);

        return new RhySubsidyStage1DTO(
                new RhyAndRkaDTO(statistics.getOrganisation(), statistics.getParentOrganisation()),
                new SubsidyCalculationStage1DTO(
                        calculatedShares,
                        roundTotalSubsidyShare(calculatedShares.countSumOfAllShares())));
    }

    private SubsidyAllocationStage1Calculation() {
        throw new AssertionError();
    }
}
