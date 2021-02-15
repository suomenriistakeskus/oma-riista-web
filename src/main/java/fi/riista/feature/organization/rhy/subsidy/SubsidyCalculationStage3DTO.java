package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class SubsidyCalculationStage3DTO extends SubsidyCalculationStage2DTO {

    private final SubsidyComparisonToLastYearDTO subsidyComparisonToLastYear;

    public SubsidyCalculationStage3DTO(@Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                       @Nonnull final BigDecimal totalRoundedShareBasedOnStatistics,
                                       final int remainderEurosGivenInStage2,
                                       @Nonnull final SubsidyComparisonToLastYearDTO subsidyComparisonToLastYear) {

        super(calculatedShares, totalRoundedShareBasedOnStatistics, remainderEurosGivenInStage2);

        this.subsidyComparisonToLastYear = requireNonNull(subsidyComparisonToLastYear);
    }

    // Accessors -->

    public SubsidyComparisonToLastYearDTO getSubsidyComparisonToLastYear() {
        return subsidyComparisonToLastYear;
    }
}
