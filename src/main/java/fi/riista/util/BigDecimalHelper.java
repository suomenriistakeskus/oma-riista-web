package fi.riista.util;

import java.math.BigDecimal;

public class BigDecimalHelper {

    public static fi.riista.util.BigDecimalHelper of(BigDecimal v) {
        return new fi.riista.util.BigDecimalHelper(v);
    }

    private final BigDecimal value;

    public BigDecimalHelper(BigDecimal v) {
        this.value = v;
    }

    public boolean gt(BigDecimal o) {
        return value.compareTo(o) > 0;
    }

    public boolean gte(BigDecimal o) {
        return value.compareTo(o) >= 0;
    }

    public boolean lt(BigDecimal o) {
        return value.compareTo(o) < 0;
    }

    public boolean lte(BigDecimal o) {
        return value.compareTo(o) <= 0;
    }

    public boolean eq(BigDecimal o) {
        return value.compareTo(o) == 0;
    }

    public boolean betweenOrEqual(BigDecimal min, BigDecimal max) {
        return gte(min) && lte(max);
    }

    public boolean between(BigDecimal min, BigDecimal max) {
        return gt(min) && lt(max);
    }

    public BigDecimal getValue() {
        return value;
    }

}
