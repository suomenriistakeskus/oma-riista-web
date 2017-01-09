package fi.riista.feature.organization.person;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.util.DateUtil.today;
import static fi.riista.util.TestUtils.ld;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PersonEntityTest {
    private static final String INVOICE_REFERENCE1 = "14507700161";
    private static final String INVOICE_REFERENCE2 = "15862640169";

    @Test
    public void testMaskSSN() {
        assertEquals("123456*****", Person.maskSsn("123456A0123"));
    }

    @Test
    public void testMaskSSNTooShort() {
        assertEquals("1234*****", Person.maskSsn("1234"));
    }

    @Test
    public void testMaskSSNNull() {
        assertEquals("", Person.maskSsn(null));
    }

    @Test
    public void testIsHuntingCardValidNow_PaymentValid() {
        doTestHuntingCard(today().minusMonths(6), today().plusMonths(6), true);
    }

    @Test
    public void testIsHuntingCardValidNow_PaymentValid2() {
        doTestHuntingCard(today(), today().plusMonths(6), true);
    }

    @Test
    public void testIsHuntingCardValidNow_PaymentValid3() {
        doTestHuntingCard(today().minusMonths(6), today(), true);
    }

    @Test
    public void testIsHuntingCardValidNow_NotValidPayment() {
        doTestHuntingCard(today().plusDays(1), today().plusMonths(6), false);
    }

    @Test
    public void testIsHuntingCardValidNow_NotValidPayment2() {
        doTestHuntingCard(today().minusMonths(6), today().minusDays(1), false);
    }

    private static void doTestHuntingCard(LocalDate begin, LocalDate end, boolean isPaid) {
        Person p = new Person();
        p.setHuntingCardStart(begin);
        p.setHuntingCardEnd(end);

        assertEquals(isPaid, p.isHuntingCardValidNow());
    }

    @Test
    public void testIsHunterExamValidNow() {
        final LocalDate today = today();

        doTestHunterExam(null, null, false);

        doTestHunterExam(today, null, true);
        doTestHunterExam(today.plusDays(1), null, false);
        doTestHunterExam(today.minusDays(1), null, true);

        doTestHunterExam(null, today, true);
        doTestHunterExam(null, today.plusDays(1), true);
        doTestHunterExam(null, today.minusDays(1), false);

        doTestHunterExam(today, today, true);
        doTestHunterExam(today, today.plusDays(1), true);

        doTestHunterExam(today.plusDays(1), today.plusDays(1), false);

        doTestHunterExam(today.minusDays(1), today, true);
        doTestHunterExam(today.minusDays(1), today.plusDays(1), true);
        doTestHunterExam(today.minusDays(1), today.minusDays(1), false);
    }

    private static void doTestHunterExam(LocalDate begin, LocalDate end, boolean isValid) {
        Person p = new Person();
        p.setHunterExamDate(begin);
        p.setHunterExamExpirationDate(end);

        assertEquals(isValid, p.isHunterExamValidNow());
    }

    @Test
    public void isBanActive_no() {
        doTestBan(null, null, false);
    }

    @Test
    public void isBanActive_no2() {
        doTestBan(today(), null, false);
    }

    @Test
    public void isBanActive_no3() {
        doTestBan(null, today(), false);
    }

    @Test
    public void isBanActive_banned() {
        doTestBan(today(), today().plusDays(1), true);
    }

    @Test
    public void isBanActive_banned2() {
        doTestBan(today().minusDays(1), today(), true);
    }

    private static void doTestBan(LocalDate begin, LocalDate end, boolean isBan) {
        Person p = new Person();
        p.setHuntingBanStart(begin);
        p.setHuntingBanEnd(end);

        assertEquals(isBan, p.isHuntingBanActiveNow());
    }

    @Test
    public void testPaymentDate() {
        Person p = new Person();
        p.setHuntingPaymentOneYear(2015);
        p.setHuntingPaymentOneDay(ld(2015, 6, 9));

        assertTrue(p.getHuntingPaymentDateForNextOrCurrentSeason(2015).isPresent());
        assertFalse(p.getHuntingPaymentDateForNextOrCurrentSeason(2016).isPresent());

        assertEquals(ld(2015, 6, 9), p.getHuntingPaymentDateForNextOrCurrentSeason(2015).get());
    }

    @Test
    public void testPaymentDate_PreferNextSeason() {
        Person p = new Person();
        p.setHuntingPaymentOneYear(2015);
        p.setHuntingPaymentOneDay(ld(2015, 6, 9));
        p.setHuntingPaymentTwoYear(2016);
        p.setHuntingPaymentTwoDay(ld(2016, 6, 9));

        assertTrue(p.getHuntingPaymentDateForNextOrCurrentSeason(2014).isPresent());
        assertTrue(p.getHuntingPaymentDateForNextOrCurrentSeason(2015).isPresent());
        assertTrue(p.getHuntingPaymentDateForNextOrCurrentSeason(2016).isPresent());

        assertEquals(ld(2015, 6, 9), p.getHuntingPaymentDateForNextOrCurrentSeason(2014).get());
        assertEquals(ld(2016, 6, 9), p.getHuntingPaymentDateForNextOrCurrentSeason(2015).get());
        assertEquals(ld(2016, 6, 9), p.getHuntingPaymentDateForNextOrCurrentSeason(2016).get());
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_NoInvoiceReference() {
        Person p = new Person();
        assertEquals(ImmutableSet.of(), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_NextYearReference() {
        final int currentHuntingYear = 2015;
        final int nextHuntingYear = currentHuntingYear + 1;

        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(nextHuntingYear);

        assertEquals("next hunting year is included", ImmutableSet.of(nextHuntingYear), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_BothYearReference() {
        final int currentHuntingYear = 2015;
        final int nextHuntingYear = currentHuntingYear + 1;

        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(nextHuntingYear);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(currentHuntingYear);

        assertEquals("both hunting years are included", ImmutableSet.of(currentHuntingYear, nextHuntingYear), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_CurrentYearReference() {
        final int currentHuntingYear = 2015;

        Person p = new Person();
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE1);
        p.setInvoiceReferencePreviousYear(currentHuntingYear);

        assertEquals("current year reference is included", ImmutableSet.of(currentHuntingYear), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_OldYearReference() {
        final int currentHuntingYear = 2015;
        final int oldHuntingYear = currentHuntingYear - 1;

        Person p = new Person();
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE1);
        p.setInvoiceReferencePreviousYear(oldHuntingYear);

        assertEquals("old reference is ignored", ImmutableSet.of(), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_OldPayment_BothYearReference() {
        final int currentHuntingYear = 2015;
        final int nextHuntingYear = currentHuntingYear + 1;

        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(nextHuntingYear);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(currentHuntingYear);
        p.setHuntingPaymentOneYear(2012);
        p.setHuntingPaymentOneDay(ld(2012, 6, 9));
        p.setHuntingPaymentTwoYear(2013);
        p.setHuntingPaymentTwoDay(ld(2013, 6, 9));

        assertEquals("both hunting years are included", ImmutableSet.of(currentHuntingYear, nextHuntingYear), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_CurrentPayment_BothYearReference() {
        final int currentHuntingYear = 2015;
        final int nextHuntingYear = currentHuntingYear + 1;

        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(nextHuntingYear);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(currentHuntingYear);
        p.setHuntingPaymentOneYear(currentHuntingYear);
        p.setHuntingPaymentOneDay(ld(currentHuntingYear, 6, 9));

        assertEquals("both hunting years are included", ImmutableSet.of(nextHuntingYear), p.getHuntingPaymentPdfYears(2015));
    }

    @Test
    public void testHuntingPaymentPdfYears_AllPayments_BothReferences() {
        final int currentHuntingYear = 2015;
        final int nextHuntingYear = currentHuntingYear + 1;

        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(nextHuntingYear);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(currentHuntingYear);
        p.setHuntingPaymentOneYear(nextHuntingYear);
        p.setHuntingPaymentOneDay(ld(nextHuntingYear, 6, 9));
        p.setHuntingPaymentTwoYear(currentHuntingYear);
        p.setHuntingPaymentTwoDay(ld(currentHuntingYear, 6, 9));

        assertEquals("both hunting years are excluded", ImmutableSet.of(), p.getHuntingPaymentPdfYears(2015));
    }
}
