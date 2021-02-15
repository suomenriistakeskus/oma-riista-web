package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class SubsidyCalculationStage4DTO extends SubsidyCalculationStage3DTO {

    private final SubsidyRoundingDTO stage4Rounding;

    public SubsidyCalculationStage4DTO(@Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                       @Nonnull final BigDecimal subsidyAfterStage2RemainderAllocation,
                                       final int remainderEurosGivenInStage2,
                                       @Nonnull final SubsidyComparisonToLastYearDTO subsidyComparisonToLastYear,
                                       @Nonnull final SubsidyRoundingDTO stage4Rounding) {

        super(calculatedShares, subsidyAfterStage2RemainderAllocation, remainderEurosGivenInStage2, subsidyComparisonToLastYear);

        this.stage4Rounding = requireNonNull(stage4Rounding);
    }

    // Accessors -->

    public SubsidyRoundingDTO getStage4Rounding() {
        return stage4Rounding;
    }
}
