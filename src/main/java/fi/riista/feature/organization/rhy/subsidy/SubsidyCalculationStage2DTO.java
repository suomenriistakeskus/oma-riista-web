package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SubsidyCalculationStage2DTO {

    private final StatisticsBasedSubsidyShareDTO calculatedShares;

    private final BigDecimal subsidyAfterStage2RemainderAllocation;

    // Even remainder euros allocated within rounding operations.
    private final int remainderEurosGivenInStage2;

    public SubsidyCalculationStage2DTO(@Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                       @Nonnull final BigDecimal subsidyAfterStage2RemainderAllocation,
                                       final int remainderEurosGivenInStage2) {

        this.calculatedShares = requireNonNull(calculatedShares);

        this.subsidyAfterStage2RemainderAllocation = requireNonNull(subsidyAfterStage2RemainderAllocation);

        checkArgument(remainderEurosGivenInStage2 >= 0, "Remainder euros in stage 2 must not be negative");
        this.remainderEurosGivenInStage2 = remainderEurosGivenInStage2;
    }

    // Accessors -->

    public StatisticsBasedSubsidyShareDTO getCalculatedShares() {
        return calculatedShares;
    }

    
    public BigDecimal getSubsidyAfterStage2RemainderAllocation() {
        return subsidyAfterStage2RemainderAllocation;
    }

    public int getRemainderEurosGivenInStage2() {
        return remainderEurosGivenInStage2;
    }
}
