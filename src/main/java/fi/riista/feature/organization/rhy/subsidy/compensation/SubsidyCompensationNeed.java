package fi.riista.feature.organization.rhy.subsidy.compensation;

import java.math.BigDecimal;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.util.NumberUtils.isPositive;
import static java.math.BigDecimal.ZERO;

public interface SubsidyCompensationNeed {

    BigDecimal countDifferenceOfTotalCalculatedSubsidyToLowerLimit();

    BigDecimal getCalculatedSubsidyForSecondBatch();

    default boolean isExactlyAtLowerLimit() {
        return countDifferenceOfTotalCalculatedSubsidyToLowerLimit().compareTo(ZERO) == 0;
    }

    // Returns zero if there is no need for compensation.
    default BigDecimal countAmountOfCompensationNeed() {
        return ZERO_MONETARY_AMOUNT
                .min(countDifferenceOfTotalCalculatedSubsidyToLowerLimit())
                .min(getCalculatedSubsidyForSecondBatch())
                .negate();
    }

    default boolean isCompensationNeeded() {
        return isPositive(countAmountOfCompensationNeed());
    }
}
