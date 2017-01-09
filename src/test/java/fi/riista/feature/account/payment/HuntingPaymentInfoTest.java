package fi.riista.feature.account.payment;

import fi.riista.feature.account.payment.HuntingPaymentInfo;
import org.iban4j.Bic;
import org.iban4j.BicFormatException;
import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.joda.time.LocalDate;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HuntingPaymentInfoTest {

    private static final String INVOICE_REFERENCE = "14507700161";
    private static final String BANK_NAME = "Bank Oy Ab";

    @Test
    public void testSmoke() {
        verify(HuntingPaymentInfo.create(2015, INVOICE_REFERENCE));
        verify(HuntingPaymentInfo.create(2016, INVOICE_REFERENCE));
    }

    private static void verify(final HuntingPaymentInfo paymentInfo) {
        assertNotNull(paymentInfo);
        assertEquals("33.00", paymentInfo.getAmountText());
        assertEquals("" +
                        "OP-Pohjola   FI78 5000 0120 3784 42\n" +
                        "Nordea       FI12 1660 3000 1072 12\n" +
                        "Danske       FI84 8000 1300 0353 50",
                paymentInfo.getPaymentReceiverIban());
        assertEquals("7850000120378442", paymentInfo.getIbanForBarCode());
        assertEquals("OKOYFIHH\n" +
                        "NDEAFIHH\n" +
                        "DABAFIHH",
                paymentInfo.getPaymentReceiverBic());
        assertEquals("00000000014507700161", paymentInfo.getInvoiceReferenceForBarCode());
        assertEquals("1 45077 00161", paymentInfo.getInvoiceReferenceForHuman());
        assertEquals("478500001203784420000330000000000000014507700161000000",
                paymentInfo.createBarCodeMessage(null));
    }

    @Test
    public void testNotAvailabeForInvalidYear() {
        assertNull(HuntingPaymentInfo.create(2014, INVOICE_REFERENCE));
        assertNull(HuntingPaymentInfo.create(2017, INVOICE_REFERENCE));
        assertNull(HuntingPaymentInfo.create(2018, INVOICE_REFERENCE));
    }

    @Test
    public void testEurosAndCents() {
        final HuntingPaymentInfo paymentInfo = new HuntingPaymentInfo(
                77, 99, INVOICE_REFERENCE, singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), BANK_NAME)));

        assertEquals("77.99", paymentInfo.getAmountText());
        assertEquals("478500001203784420000779900000000000014507700161000000",
                paymentInfo.createBarCodeMessage(null));
    }

    @Test
    public void testBarCodeDueDate() {
        final HuntingPaymentInfo paymentInfo = new HuntingPaymentInfo(
                77, 99, INVOICE_REFERENCE, singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), BANK_NAME)));

        assertEquals("77.99", paymentInfo.getAmountText());
        assertEquals("478500001203784420000779900000000000014507700161160630",
                paymentInfo.createBarCodeMessage(new LocalDate(2016, 6, 30)));
    }

    @Test(expected = BicFormatException.class)
    public void testInvalidBic() {
        new HuntingPaymentInfo(
                77, 99, INVOICE_REFERENCE, singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("INVALID"), Iban.valueOf("FI7850000120378442"), BANK_NAME)));
    }

    @Test(expected = IbanFormatException.class)
    public void testInvalidIban() {
        new HuntingPaymentInfo(
                77, 99, INVOICE_REFERENCE, singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("OKOYFIHH"), Iban.valueOf("FIX7850000120378442"), BANK_NAME)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCents() {
        new HuntingPaymentInfo(
                77, 100, INVOICE_REFERENCE, singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), BANK_NAME)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEuros() {
        new HuntingPaymentInfo(
                -1, 0, INVOICE_REFERENCE, singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), BANK_NAME)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInvoiceReference() {
        new HuntingPaymentInfo(
                33, 0, INVOICE_REFERENCE + "1", singletonList(new HuntingPaymentInfo.AccountDetails(
                Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), BANK_NAME)));
    }
}
