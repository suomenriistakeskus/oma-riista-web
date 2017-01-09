package fi.riista.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BigDecimalHelperTest {

    @Test
    public void testGt() {
        assertTrue(BigDecimalHelper.of(bd(10)).gt(bd(9)));
        assertFalse(BigDecimalHelper.of(bd(10)).gt(bd(10)));
        assertFalse(BigDecimalHelper.of(bd(10)).gt(bd(11)));
    }

    @Test
    public void testGte() {
        assertTrue(BigDecimalHelper.of(bd(10)).gte(bd(9)));
        assertTrue(BigDecimalHelper.of(bd(10)).gte(bd(10)));
        assertFalse(BigDecimalHelper.of(bd(10)).gte(bd(11)));
    }

    @Test
    public void testLt() {
        assertFalse(BigDecimalHelper.of(bd(10)).lt(bd(9)));
        assertFalse(BigDecimalHelper.of(bd(10)).lt(bd(10)));
        assertTrue(BigDecimalHelper.of(bd(10)).lt(bd(11)));
    }

    @Test
    public void testLte() {
        assertFalse(BigDecimalHelper.of(bd(10)).lte(bd(9)));
        assertTrue(BigDecimalHelper.of(bd(10)).lte(bd(10)));
        assertTrue(BigDecimalHelper.of(bd(10)).lte(bd(11)));
    }

    @Test
    public void testEq() {
        assertFalse(BigDecimalHelper.of(bd(10)).eq(bd(9)));
        assertTrue(BigDecimalHelper.of(bd(10)).eq(bd(10)));
        assertFalse(BigDecimalHelper.of(bd(10)).eq(bd(11)));
    }

    @Test
    public void testBetweenOrEqual() {
        assertFalse(BigDecimalHelper.of(bd(10)).betweenOrEqual(bd(9), bd(9)));
        assertTrue(BigDecimalHelper.of(bd(10)).betweenOrEqual(bd(9), bd(10)));
        assertTrue(BigDecimalHelper.of(bd(10)).betweenOrEqual(bd(9), bd(11)));
        assertTrue(BigDecimalHelper.of(bd(10)).betweenOrEqual(bd(9), bd(12)));
        assertTrue(BigDecimalHelper.of(bd(10)).betweenOrEqual(bd(10), bd(12)));
        assertFalse(BigDecimalHelper.of(bd(10)).betweenOrEqual(bd(11), bd(12)));
    }

    @Test
    public void testBetween() {
        assertFalse(BigDecimalHelper.of(bd(10)).between(bd(9), bd(9)));
        assertFalse(BigDecimalHelper.of(bd(10)).between(bd(9), bd(10)));
        assertTrue(BigDecimalHelper.of(bd(10)).between(bd(9), bd(11)));
        assertTrue(BigDecimalHelper.of(bd(10)).between(bd(9), bd(12)));
        assertFalse(BigDecimalHelper.of(bd(10)).between(bd(10), bd(12)));
        assertFalse(BigDecimalHelper.of(bd(10)).between(bd(11), bd(12)));
    }

    private static BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

}