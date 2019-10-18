package fi.riista.feature.organization.rhy.subsidy;

import java.math.BigDecimal;

public final class SubsidyAllocationConstants {

    public static final int FIRST_SUBSIDY_YEAR = 2019;

    public static final int DEFAULT_ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;

    public static final BigDecimal MAX_ANNUAL_SUBSIDY_DECREASE_COEFFICIENT = new BigDecimal(80).movePointLeft(2);

    private SubsidyAllocationConstants() {
        throw new AssertionError();
    }
}
