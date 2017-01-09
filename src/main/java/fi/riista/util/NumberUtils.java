package fi.riista.util;

import java.math.BigDecimal;

public final class NumberUtils {

    private static final double EPSILON = 0.00001;

    private NumberUtils() {
        throw new AssertionError();
    }

    public static boolean equal(final Float num1, final Float num2) {
        return num1 == null ? num2 == null : num2 != null && Math.abs(num1 - num2) < EPSILON;
    }

    public static boolean equal(final Double num1, final Double num2) {
        return num1 == null ? num2 == null : num2 != null && Math.abs(num1 - num2) < EPSILON;
    }

    public static void bigDecimalEquals(long expected, BigDecimal actual) {
        NumberUtils.bigDecimalEquals(BigDecimal.valueOf(expected), actual);
    }

    public static void bigDecimalEquals(int expected, BigDecimal actual) {
        NumberUtils.bigDecimalEquals(BigDecimal.valueOf(expected), actual);
    }

    public static void bigDecimalEquals(BigDecimal expected, long actual) {
        NumberUtils.bigDecimalEquals(expected, BigDecimal.valueOf(actual));
    }

    public static void bigDecimalEquals(BigDecimal expected, int actual) {
        NumberUtils.bigDecimalEquals(expected, BigDecimal.valueOf(actual));
    }

    public static void bigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        final boolean eitherNull = expected == null && actual != null || expected != null && actual == null;

        if (eitherNull || expected != null && actual != null && expected.compareTo(actual) != 0) {
            throw new AssertionError(String.format("expected %s but was: %s", expected, actual));
        }
    }

}
