package fi.riista.feature.account.payment;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import fi.riista.feature.common.money.FinnishBankAccount;
import java.util.List;
import org.iban4j.IbanFormatException;
import org.joda.time.LocalDate;
import org.junit.Test;

public class HuntingPaymentInfoTest {

    private static final String IBAN = "FI7850000120378442";
    private static final String INVOICE_REFERENCE = "14507700161";

    private static final List<FinnishBankAccount> SINGLE_BANK_ACCOUNT =
            singletonList(FinnishBankAccount.fromIban(IBAN));

    @Test
    public void testYear2023_Adult() {
        final LocalDate dateOfBirth = new LocalDate(2005, 7, 31);
        final HuntingPaymentInfo paymentInfo = HuntingPaymentInfo.create(2023, dateOfBirth, INVOICE_REFERENCE);
        assertNotNull(paymentInfo);
        verifyCommonPaymentInfo(paymentInfo);
        assertEquals("43.00", paymentInfo.getAmountText());
        assertEquals("484800013000353500000430000000000000014507700161000000",
                paymentInfo.createBarCodeMessage(null));
    }

    @Test
    public void testYear2023_Underage() {
        final LocalDate dateOfBirth = new LocalDate(2005, 8, 1);
        final HuntingPaymentInfo paymentInfo = HuntingPaymentInfo.create(2023, dateOfBirth, INVOICE_REFERENCE);
        assertNotNull(paymentInfo);
        verifyCommonPaymentInfo(paymentInfo);
        assertEquals("10.00", paymentInfo.getAmountText());
        assertEquals("484800013000353500000100000000000000014507700161000000",
                paymentInfo.createBarCodeMessage(null));
    }

    @Test
    public void testYear2022_Adult() {
        final LocalDate dateOfBirth = new LocalDate(2004, 7, 31);
        final HuntingPaymentInfo paymentInfo = HuntingPaymentInfo.create(2022, dateOfBirth, INVOICE_REFERENCE);
        assertNotNull(paymentInfo);
        verifyCommonPaymentInfo(paymentInfo);
        assertEquals("39.00", paymentInfo.getAmountText());
        assertEquals("484800013000353500000390000000000000014507700161000000",
                paymentInfo.createBarCodeMessage(null));
    }

    @Test
    public void testYear2022_Underage() {
        final LocalDate dateOfBirth = new LocalDate(2004, 8, 1);
        final HuntingPaymentInfo paymentInfo = HuntingPaymentInfo.create(2022, dateOfBirth, INVOICE_REFERENCE);
        assertNotNull(paymentInfo);
        verifyCommonPaymentInfo(paymentInfo);
        assertEquals("20.00", paymentInfo.getAmountText());
        assertEquals("484800013000353500000200000000000000014507700161000000",
                paymentInfo.createBarCodeMessage(null));
    }

    private static void verifyCommonPaymentInfo(final HuntingPaymentInfo paymentInfo) {
        assertEquals("" +
                        "Danske       FI84 8000 1300 0353 50\n" +
                        "Nordea       FI12 1660 3000 1072 12",
                paymentInfo.getPaymentReceiverIban());
        assertEquals("FI8480001300035350", paymentInfo.getIbanForBarCode().toString());
        assertEquals("" +
                        "DABAFIHH\n" +
                        "NDEAFIHH",
                paymentInfo.getPaymentReceiverBic());
        assertEquals("1 45077 00161", paymentInfo.getInvoiceReferenceForHuman());
    }

    private static void verifyOldPaymentInfo(final HuntingPaymentInfo paymentInfo) {
        assertEquals("" +
                        "OP           FI78 5000 0120 3784 42\n" +
                        "Nordea       FI12 1660 3000 1072 12\n" +
                        "Danske       FI84 8000 1300 0353 50",
                paymentInfo.getPaymentReceiverIban());
        assertEquals("FI7850000120378442", paymentInfo.getIbanForBarCode().toString());
        assertEquals("" +
                        "OKOYFIHH\n" +
                        "NDEAFIHH\n" +
                        "DABAFIHH",
                paymentInfo.getPaymentReceiverBic());
        assertEquals("1 45077 00161", paymentInfo.getInvoiceReferenceForHuman());
    }

    @Test
    public void testEurosAndCents() {
        final HuntingPaymentInfo paymentInfo = new HuntingPaymentInfo(77, 99, INVOICE_REFERENCE, SINGLE_BANK_ACCOUNT);

        assertEquals("77.99", paymentInfo.getAmountText());
        assertEquals("478500001203784420000779900000000000014507700161000000", paymentInfo.createBarCodeMessage(null));
    }

    @Test
    public void testBarCodeDueDate() {
        final HuntingPaymentInfo paymentInfo = new HuntingPaymentInfo(77, 99, INVOICE_REFERENCE, SINGLE_BANK_ACCOUNT);

        assertEquals("77.99", paymentInfo.getAmountText());
        assertEquals("478500001203784420000779900000000000014507700161160630",
                paymentInfo.createBarCodeMessage(new LocalDate(2016, 6, 30)));
    }

    @Test(expected = IbanFormatException.class)
    public void testInvalidIban() {
        new HuntingPaymentInfo(
                77, 99, INVOICE_REFERENCE, singletonList(FinnishBankAccount.fromIban("FIX7850000120378442")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCents() {
        new HuntingPaymentInfo(77, 100, INVOICE_REFERENCE, SINGLE_BANK_ACCOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEuros() {
        new HuntingPaymentInfo(-1, 0, INVOICE_REFERENCE, SINGLE_BANK_ACCOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInvoiceReference() {
        new HuntingPaymentInfo(33, 0, INVOICE_REFERENCE + "1", SINGLE_BANK_ACCOUNT);
    }
}
