package fi.riista.feature.account.payment;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.feature.account.payment.HuntingPaymentUtil.getHuntingPaymentPdfYears;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuntingPaymentUtilTest {
    private static final String INVOICE_REFERENCE1 = "14507700161";
    private static final String INVOICE_REFERENCE2 = "15862640169";

    private static int HUNTING_YEAR = 2019;
    private static int PREVIOUS_HUNTING_YEAR = HUNTING_YEAR - 1;

    // Valid payment date for both current and next year (between 15.7 and 31.7)
    private static LocalDate TODAY = new LocalDate(HUNTING_YEAR, 7, 20);

    @Test
    public void testMakeSurePaymentDetailsAreAvailableOnTime() {
        // When this test fails update all constants above to match current and next hunting year
        assertTrue(today().isBefore(new LocalDate(HUNTING_YEAR + 1, 6, 1)));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_NoInvoiceReference() {
        Person p = new Person();
        assertEquals(ImmutableSet.of(), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_NextYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(HUNTING_YEAR);

        assertEquals("next hunting year is included",
                ImmutableSet.of(HUNTING_YEAR), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_BothYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(PREVIOUS_HUNTING_YEAR);

        assertEquals("both hunting years are included",
                ImmutableSet.of(PREVIOUS_HUNTING_YEAR, HUNTING_YEAR), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_CurrentYearReference() {
        Person p = new Person();
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE1);
        p.setInvoiceReferencePreviousYear(PREVIOUS_HUNTING_YEAR);

        assertEquals("current year reference is included",
                ImmutableSet.of(PREVIOUS_HUNTING_YEAR), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_OldYearReference() {
        Person p = new Person();
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE1);
        p.setInvoiceReferencePreviousYear(PREVIOUS_HUNTING_YEAR - 1);

        assertEquals("old reference is ignored", ImmutableSet.of(), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_OldPayment_BothYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(PREVIOUS_HUNTING_YEAR);
        p.setHuntingPaymentOneYear(2012);
        p.setHuntingPaymentOneDay(ld(2012, 6, 9));
        p.setHuntingPaymentTwoYear(2013);
        p.setHuntingPaymentTwoDay(ld(2013, 6, 9));

        assertEquals("both hunting years are included",
                ImmutableSet.of(PREVIOUS_HUNTING_YEAR, HUNTING_YEAR), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_CurrentPayment_BothYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(PREVIOUS_HUNTING_YEAR);
        p.setHuntingPaymentOneYear(PREVIOUS_HUNTING_YEAR);
        p.setHuntingPaymentOneDay(ld(PREVIOUS_HUNTING_YEAR, 6, 9));

        assertEquals("both hunting years are included",
                ImmutableSet.of(HUNTING_YEAR), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_AllPayments_BothReferences() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(PREVIOUS_HUNTING_YEAR);
        p.setHuntingPaymentOneYear(HUNTING_YEAR);
        p.setHuntingPaymentOneDay(ld(HUNTING_YEAR, 6, 9));
        p.setHuntingPaymentTwoYear(PREVIOUS_HUNTING_YEAR);
        p.setHuntingPaymentTwoDay(ld(PREVIOUS_HUNTING_YEAR, 6, 9));

        assertEquals("both hunting years are excluded",
                ImmutableSet.of(), getHuntingPaymentPdfYears(TODAY, p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NotBeforeMagazineReleased() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(PREVIOUS_HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(HUNTING_YEAR);

        assertEquals("only current",
                ImmutableSet.of(PREVIOUS_HUNTING_YEAR), getHuntingPaymentPdfYears(
                        new LocalDate(HUNTING_YEAR, 7, 14), p));

        assertEquals("both",
                ImmutableSet.of(PREVIOUS_HUNTING_YEAR, HUNTING_YEAR), getHuntingPaymentPdfYears(
                        new LocalDate(HUNTING_YEAR, 7, 15), p));

        assertEquals("both",
                ImmutableSet.of(PREVIOUS_HUNTING_YEAR, HUNTING_YEAR), getHuntingPaymentPdfYears(
                        new LocalDate(HUNTING_YEAR, 7, 31), p));

        assertEquals("only next",
                ImmutableSet.of(HUNTING_YEAR), getHuntingPaymentPdfYears(
                        new LocalDate(HUNTING_YEAR, 8, 1), p));
    }
}
