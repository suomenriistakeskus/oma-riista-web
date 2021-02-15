package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class SubsidyCalculationStage1DTO {

    private final StatisticsBasedSubsidyShareDTO calculatedShares;
    private final BigDecimal totalRoundedShare;

    public SubsidyCalculationStage1DTO(@Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                       @Nonnull final BigDecimal totalRoundedShare) {

        this.calculatedShares = requireNonNull(calculatedShares);
        this.totalRoundedShare = requireNonNull(totalRoundedShare);
    }

    // Accessors -->

    public StatisticsBasedSubsidyShareDTO getCalculatedShares() {
        return calculatedShares;
    }

    public BigDecimal getTotalRoundedShare() {
        return totalRoundedShare;
    }
}
