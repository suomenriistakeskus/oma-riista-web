package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static fi.riista.util.NumberUtils.nullableSum;
import static fi.riista.util.NumberUtils.sum;
import static java.util.Objects.requireNonNull;

public class SubsidyCalculationStage5DTO extends SubsidyCalculationStage4DTO {

    private final BigDecimal subsidyOfBatch1;
    private final BigDecimal subsidyOfBatch2;

    // Produces a summary over given iterable of allocations. All numeric amounts are added together.
    public static SubsidyCalculationStage5DTO aggregate(@Nonnull final Iterable<SubsidyCalculationStage5DTO> calculations) {
        requireNonNull(calculations);

        final StatisticsBasedSubsidyShareDTO sumOfCalculatedShares = StatisticsBasedSubsidyShareDTO.aggregate(
                F.mapNonNullsToList(calculations, SubsidyCalculationStage5DTO::getCalculatedShares));

        final BigDecimal sumOfSubsidiesAfterStage2RemainderAllocation =
                nullableSum(calculations, SubsidyCalculationStage5DTO::getSubsidyAfterStage2RemainderAllocation);

        final int sumOfRemainderEurosInStage2 =
                sum(calculations, SubsidyCalculationStage5DTO::getRemainderEurosGivenInStage2);

        final SubsidyComparisonToLastYearDTO aggregateOfSubsidyComparisons = SubsidyComparisonToLastYearDTO.aggregate(
                F.mapNonNullsToList(calculations, SubsidyCalculationStage5DTO::getSubsidyComparisonToLastYear));

        final SubsidyRoundingDTO aggregateOfSubsidyRoundings = SubsidyRoundingDTO.aggregate(
                F.mapNonNullsToList(calculations, SubsidyCalculationStage5DTO::getStage4Rounding));

        final BigDecimal sumOfBatch1Subsidies =
                nullableSum(calculations, SubsidyCalculationStage5DTO::getSubsidyOfBatch1);

        final BigDecimal sumOfBatch2Subsidies =
                nullableSum(calculations, SubsidyCalculationStage5DTO::getSubsidyOfBatch2);

        return new SubsidyCalculationStage5DTO(
                sumOfCalculatedShares,
                sumOfSubsidiesAfterStage2RemainderAllocation,
                sumOfRemainderEurosInStage2,
                aggregateOfSubsidyComparisons,
                aggregateOfSubsidyRoundings,
                sumOfBatch1Subsidies,
                sumOfBatch2Subsidies);
    }


    public SubsidyCalculationStage5DTO(@Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                       @Nonnull final BigDecimal subsidyAfterStage2RemainderAllocation,
                                       final int remainderEurosGivenInStage2,
                                       @Nonnull final SubsidyComparisonToLastYearDTO subsidyComparisonToLastYear,
                                       @Nonnull final SubsidyRoundingDTO stage4Rounding,
                                       final BigDecimal subsidyOfBatch1,
                                       final BigDecimal subsidyOfBatch2) {

        super(calculatedShares,
                subsidyAfterStage2RemainderAllocation,
                remainderEurosGivenInStage2,
                subsidyComparisonToLastYear,
                stage4Rounding);

        this.subsidyOfBatch1 = requireNonNull(subsidyOfBatch1);
        this.subsidyOfBatch2 = requireNonNull(subsidyOfBatch2);
    }

    // Accessors -->

    public BigDecimal getSubsidyOfBatch1() {
        return subsidyOfBatch1;
    }

    public BigDecimal getSubsidyOfBatch2() {
        return subsidyOfBatch2;
    }
}
