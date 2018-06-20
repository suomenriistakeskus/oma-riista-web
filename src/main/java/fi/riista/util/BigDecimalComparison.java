package fi.riista.util;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class BigDecimalComparison {

    public static fi.riista.util.BigDecimalComparison of(final BigDecimal o) {
        return new fi.riista.util.BigDecimalComparison(o);
    }

    public static boolean nullsafeEq(@Nullable final BigDecimal o1, @Nullable final BigDecimal o2) {
        if (o1 == null) {
            return o2 == null;
        }

        return o2 != null && new fi.riista.util.BigDecimalComparison(o1).eq(o2);
    }

    private final BigDecimal value;

    public BigDecimalComparison(final BigDecimal v) {
        this.value = v;
    }

    public boolean gt(final BigDecimal o) {
        return value.compareTo(o) > 0;
    }

    public boolean gte(final BigDecimal o) {
        return value.compareTo(o) >= 0;
    }

    public boolean lt(final BigDecimal o) {
        return value.compareTo(o) < 0;
    }

    public boolean lte(final BigDecimal o) {
        return value.compareTo(o) <= 0;
    }

    public boolean eq(final BigDecimal o) {
        return value.compareTo(o) == 0;
    }

    public boolean betweenOrEqual(final BigDecimal min, final BigDecimal max) {
        return gte(min) && lte(max);
    }

    public boolean between(final BigDecimal min, final BigDecimal max) {
        return gt(min) && lt(max);
    }

    public BigDecimal getValue() {
        return value;
    }
}
