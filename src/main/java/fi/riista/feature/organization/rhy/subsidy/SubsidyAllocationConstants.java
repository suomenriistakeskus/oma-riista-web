package fi.riista.feature.organization.rhy.subsidy;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

public final class SubsidyAllocationConstants {

    public enum SubsidyAllocationRule {
        PREDETERMINED,
        PREVIOUS_ANNUAL_STATISTICS,
        SUM_OF_TWO_ANNUAL_STATISTICS
    }

    public static final int FIRST_SUBSIDY_YEAR = 2019;

    public static final int DEFAULT_ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;

    public static final BigDecimal MAX_ANNUAL_SUBSIDY_DECREASE_COEFFICIENT = new BigDecimal(80).movePointLeft(2);

    // RHY subsidies are annually granted in two batches. This constant defines the share (0-1)
    // of the first batch.
    public static final BigDecimal FIRST_BATCH_SHARE_COEFFICIENT = new BigDecimal(50).movePointLeft(2);

    public static SubsidyAllocationRule getAllocationRule(final int subsidyYear) {
        checkArgument(subsidyYear >= FIRST_SUBSIDY_YEAR);

        switch (subsidyYear) {
            case 2019:
                return SubsidyAllocationRule.PREDETERMINED;
            case 2020:
                return SubsidyAllocationRule.PREVIOUS_ANNUAL_STATISTICS;
            default:
                return SubsidyAllocationRule.SUM_OF_TWO_ANNUAL_STATISTICS;
        }
    }

    private SubsidyAllocationConstants() {
        throw new AssertionError();
    }
}
