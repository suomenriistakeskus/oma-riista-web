package fi.riista.util;

import org.junit.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BigDecimalComparisonTest {

    @Test
    public void testGt() {
        assertTrue(BigDecimalComparison.of(bd(10)).gt(bd(9)));
        assertFalse(BigDecimalComparison.of(bd(10)).gt(bd(10)));
        assertFalse(BigDecimalComparison.of(bd(10)).gt(bd(11)));
    }

    @Test
    public void testGte() {
        assertTrue(BigDecimalComparison.of(bd(10)).gte(bd(9)));
        assertTrue(BigDecimalComparison.of(bd(10)).gte(bd(10)));
        assertFalse(BigDecimalComparison.of(bd(10)).gte(bd(11)));
    }

    @Test
    public void testLt() {
        assertFalse(BigDecimalComparison.of(bd(10)).lt(bd(9)));
        assertFalse(BigDecimalComparison.of(bd(10)).lt(bd(10)));
        assertTrue(BigDecimalComparison.of(bd(10)).lt(bd(11)));
    }

    @Test
    public void testLte() {
        assertFalse(BigDecimalComparison.of(bd(10)).lte(bd(9)));
        assertTrue(BigDecimalComparison.of(bd(10)).lte(bd(10)));
        assertTrue(BigDecimalComparison.of(bd(10)).lte(bd(11)));
    }

    @Test
    public void testEq() {
        assertFalse(BigDecimalComparison.of(bd(10)).eq(bd(9)));
        assertTrue(BigDecimalComparison.of(bd(10)).eq(bd(10)));
        assertFalse(BigDecimalComparison.of(bd(10)).eq(bd(11)));
    }

    @Test
    public void testBetweenOrEqual() {
        assertFalse(BigDecimalComparison.of(bd(10)).betweenOrEqual(bd(9), bd(9)));
        assertTrue(BigDecimalComparison.of(bd(10)).betweenOrEqual(bd(9), bd(10)));
        assertTrue(BigDecimalComparison.of(bd(10)).betweenOrEqual(bd(9), bd(11)));
        assertTrue(BigDecimalComparison.of(bd(10)).betweenOrEqual(bd(9), bd(12)));
        assertTrue(BigDecimalComparison.of(bd(10)).betweenOrEqual(bd(10), bd(12)));
        assertFalse(BigDecimalComparison.of(bd(10)).betweenOrEqual(bd(11), bd(12)));
    }

    @Test
    public void testBetween() {
        assertFalse(BigDecimalComparison.of(bd(10)).between(bd(9), bd(9)));
        assertFalse(BigDecimalComparison.of(bd(10)).between(bd(9), bd(10)));
        assertTrue(BigDecimalComparison.of(bd(10)).between(bd(9), bd(11)));
        assertTrue(BigDecimalComparison.of(bd(10)).between(bd(9), bd(12)));
        assertFalse(BigDecimalComparison.of(bd(10)).between(bd(10), bd(12)));
        assertFalse(BigDecimalComparison.of(bd(10)).between(bd(11), bd(12)));
    }

    @Test
    public void testNullsafeEq() {
        assertTrue(BigDecimalComparison.nullsafeEq(null, null));
        assertFalse(BigDecimalComparison.nullsafeEq(ZERO, null));
        assertFalse(BigDecimalComparison.nullsafeEq(null, ZERO));

        assertTrue(BigDecimalComparison.nullsafeEq(bd(10), bd(10)));
        assertFalse(BigDecimalComparison.nullsafeEq(bd(9), bd(10)));
        assertFalse(BigDecimalComparison.nullsafeEq(bd(10), bd(11)));
    }

    private static BigDecimal bd(final int i) {
        return new BigDecimal(i);
    }
}
