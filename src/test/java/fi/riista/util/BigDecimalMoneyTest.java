package fi.riista.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class BigDecimalMoneyTest {

    @Test
    public void testFormatIntegerPaymentAmount() {
        assertEquals("90.00", new BigDecimalMoney(90,0).formatPaymentAmount());
        assertEquals("90.01", new BigDecimalMoney(90,1).formatPaymentAmount());
        assertEquals("90.99", new BigDecimalMoney(90,99).formatPaymentAmount());
        assertEquals("100.00", new BigDecimalMoney(100,0).formatPaymentAmount());
        assertEquals("100.01", new BigDecimalMoney(100,1).formatPaymentAmount());
        assertEquals("100.99", new BigDecimalMoney(100,99).formatPaymentAmount());
    }

    @Test
    public void testFormatBigDecimalPaymentAmount() {
        assertEquals("90.00", new BigDecimalMoney(new BigDecimal("90.00")).formatPaymentAmount());
        assertEquals("90.01", new BigDecimalMoney(new BigDecimal("90.01")).formatPaymentAmount());
        assertEquals("90.99", new BigDecimalMoney(new BigDecimal("90.99")).formatPaymentAmount());
        assertEquals("100.00", new BigDecimalMoney(new BigDecimal("100.00")).formatPaymentAmount());
        assertEquals("100.01", new BigDecimalMoney(new BigDecimal("100.01")).formatPaymentAmount());
        assertEquals("100.99", new BigDecimalMoney(new BigDecimal("100.99")).formatPaymentAmount());
    }

    @Test
    public void testToString() {
        assertEquals("100.99", new BigDecimalMoney(new BigDecimal("100.99")).toString());
    }
}
