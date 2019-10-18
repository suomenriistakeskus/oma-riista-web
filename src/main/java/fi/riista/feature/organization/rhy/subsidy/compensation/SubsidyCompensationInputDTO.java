package fi.riista.feature.organization.rhy.subsidy.compensation;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensationInputDTO implements SubsidyCompensationNeed {

    private final String rhyCode;

    private final BigDecimal totalSubsidyCalculatedForCurrentYear;
    private final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear;

    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    private final boolean alreadyCompensated;

    public SubsidyCompensationInputDTO(@Nonnull final String rhyCode,
                                       @Nonnull final BigDecimal totalSubsidyCalculatedForCurrentYear,
                                       @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                       @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                       final boolean alreadyCompensated) {

        this.rhyCode = requireNonNull(rhyCode);
        this.totalSubsidyCalculatedForCurrentYear = requireNonNull(totalSubsidyCalculatedForCurrentYear);
        this.subsidyGrantedInFirstBatchOfCurrentYear = requireNonNull(subsidyGrantedInFirstBatchOfCurrentYear);
        this.subsidyLowerLimitBasedOnLastYear = requireNonNull(subsidyLowerLimitBasedOnLastYear);
        this.alreadyCompensated = alreadyCompensated;

        if (alreadyCompensated) {
            checkArgument(
                    !isPositive(countAmountOfCompensationNeed()),
                    "There should be nore more compensation need after an RHY subsidy is once compensated");
        }
    }

    @Override
    public BigDecimal countDifferenceOfTotalCalculatedSubsidyToLowerLimit() {
        return totalSubsidyCalculatedForCurrentYear.subtract(subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public BigDecimal getCalculatedSubsidyForSecondBatch() {
        return totalSubsidyCalculatedForCurrentYear.subtract(subsidyGrantedInFirstBatchOfCurrentYear);
    }

    // Accessors -->

    public String getRhyCode() {
        return rhyCode;
    }

    public BigDecimal getTotalSubsidyCalculatedForCurrentYear() {
        return totalSubsidyCalculatedForCurrentYear;
    }

    public BigDecimal getSubsidyGrantedInFirstBatchOfCurrentYear() {
        return subsidyGrantedInFirstBatchOfCurrentYear;
    }

    public BigDecimal getSubsidyLowerLimitBasedOnLastYear() {
        return subsidyLowerLimitBasedOnLastYear;
    }

    public boolean isAlreadyCompensated() {
        return alreadyCompensated;
    }
}
