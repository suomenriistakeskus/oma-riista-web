package fi.riista.feature.organization.rhy.subsidy.compensation;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensationInputDTO implements SubsidyCompensationNeed {

    private final String rhyCode;

    private final BigDecimal calculatedSubsidy;

    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    private final boolean alreadyCompensated;

    public SubsidyCompensationInputDTO(@Nonnull final String rhyCode,
                                       @Nonnull final BigDecimal calculatedSubsidy,
                                       @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                       final boolean alreadyCompensated) {

        this.rhyCode = requireNonNull(rhyCode);
        this.calculatedSubsidy = requireNonNull(calculatedSubsidy);
        this.subsidyLowerLimitBasedOnLastYear = requireNonNull(subsidyLowerLimitBasedOnLastYear);
        this.alreadyCompensated = alreadyCompensated;

        if (alreadyCompensated) {
            checkArgument(
                    !isPositive(countAmountOfCompensationNeed()),
                    "There should be nore more compensation need after an RHY subsidy is once compensated");
        }
    }

    @Override
    public BigDecimal countDifferenceOfCalculatedSubsidyToLowerLimit() {
        return calculatedSubsidy.subtract(subsidyLowerLimitBasedOnLastYear);
    }

    // Accessors -->

    public String getRhyCode() {
        return rhyCode;
    }

    @Override
    public BigDecimal getCalculatedSubsidy() {
        return calculatedSubsidy;
    }

    public BigDecimal getSubsidyLowerLimitBasedOnLastYear() {
        return subsidyLowerLimitBasedOnLastYear;
    }

    public boolean isAlreadyCompensated() {
        return alreadyCompensated;
    }
}
