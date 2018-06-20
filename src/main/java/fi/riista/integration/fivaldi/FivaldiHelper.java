package fi.riista.integration.fivaldi;

import java.math.BigDecimal;

public final class FivaldiHelper {

    private FivaldiHelper() {
        throw new AssertionError();
    }

    public static BigDecimal scaleMonetaryAmount(final BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static String asString(final Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return value.toString();
    }
}
