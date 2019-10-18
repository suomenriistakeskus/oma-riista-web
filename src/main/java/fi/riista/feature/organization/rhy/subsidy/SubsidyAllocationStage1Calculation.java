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

    public static List<BasicSubsidyAllocationDTO> calculateStatisticsBasedSubsidyAllocation(
            @Nonnull final List<AnnualStatisticsExportDTO> allRhyStatistics,
            @Nonnull final List<SubsidyAllocatedToCriterionDTO> criteriaAllocations) {

        requireNonNull(criteriaAllocations);

        final Map<SubsidyAllocationCriterion, BigDecimal> unitAmountIndex =
                criteriaAllocations.stream().collect(toMap(dto -> dto.getCriterion(), dto -> dto.getUnitAmount()));

        final StatisticsBasedSubsidyShareCalculator shareCalculator =
                new StatisticsBasedSubsidyShareCalculator(unitAmountIndex);

        return F.mapNonNullsToList(allRhyStatistics, statistics -> createAllocation(statistics, shareCalculator));
    }

    private static BasicSubsidyAllocationDTO createAllocation(final AnnualStatisticsExportDTO statistics,
                                                              final StatisticsBasedSubsidyShareCalculator shareCalculator) {

        final StatisticsBasedSubsidyShareDTO calculatedShares = shareCalculator.calculateSubsidyShare(statistics);

        return new BasicSubsidyAllocationDTO(
                statistics.getOrganisation(),
                statistics.getParentOrganisation(),
                calculatedShares,
                roundTotalSubsidyShare(calculatedShares.countSumOfAllShares()),
                0); // remainder euros are not calculated in this stage
    }

    private SubsidyAllocationStage1Calculation() {
        throw new AssertionError();
    }
}
