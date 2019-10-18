package fi.riista.feature.organization.rhy.subsidy;

import java.math.BigDecimal;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.DEFAULT_ROUNDING_MODE;

// Scale amounts used in rounding operations are chosen to preserve more
// significant digits in the estimated intermediate calculation results as
// there are numbers in the total subsidy amount.
public final class SubsidyCalculation {

    public static BigDecimal round(final BigDecimal value, final int decimals) {
        return value.setScale(decimals, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal roundSubsidyAmountAllocatedToCriterion(final BigDecimal value) {
        return round(value, 3);
    }

    public static BigDecimal roundSubsidyAllocationUnitAmount(final BigDecimal value) {
        return round(value, 10);
    }

    public static BigDecimal roundSubsidyShareOfSingleCriterion(final BigDecimal value) {
        return round(value, 2);
    }

    public static BigDecimal roundToEvenEuros(final BigDecimal value) {
        return roundToEvenEuros(value, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal roundToEvenEuros(final BigDecimal value, final int roundingMode) {
        return value.setScale(0, roundingMode).setScale(2);
    }

    public static BigDecimal roundTotalSubsidyShare(final BigDecimal value) {
        return roundToEvenEuros(value, BigDecimal.ROUND_DOWN);
    }

    public static BigDecimal roundSubsidyLowerLimit(final BigDecimal value) {
        return roundToEvenEuros(value, BigDecimal.ROUND_UP);
    }

    public static BigDecimal calculateDecrement(final BigDecimal value, final BigDecimal decrementCoefficient) {
        final BigDecimal decrement = value.multiply(decrementCoefficient);
        return decrement.setScale(2, BigDecimal.ROUND_UP);
    }

    public static BigDecimal divide(final BigDecimal divisible, final BigDecimal divisor) {
        return divisible.divide(divisor, 10, DEFAULT_ROUNDING_MODE);
    }

    private SubsidyCalculation() {
        throw new AssertionError();
    }
}
