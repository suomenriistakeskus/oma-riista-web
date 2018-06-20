package fi.riista.feature.account.payment;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Set;

import static fi.riista.test.TestUtils.ld;
import static org.junit.Assert.assertEquals;

public class HuntingPaymentUtilTest {
    private static final String INVOICE_REFERENCE1 = "14507700161";
    private static final String INVOICE_REFERENCE2 = "15862640169";

    // Valid payment date for both current and next year (between 15.7 and 31.7)
    private static LocalDate TODAY = new LocalDate(2017, 7, 20);

    private static int CURRENT_HUNTING_YEAR = 2016;
    private static int NEXT_HUNTING_YEAR = 2017;

    private static Set<Integer> getHuntingPaymentPdfYear(final Person person) {
        return HuntingPaymentUtil.getHuntingPaymentPdfYears(TODAY, person);
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_NoInvoiceReference() {
        Person p = new Person();
        assertEquals(ImmutableSet.of(), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_NextYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(NEXT_HUNTING_YEAR);

        assertEquals("next hunting year is included",
                ImmutableSet.of(NEXT_HUNTING_YEAR), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_BothYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(NEXT_HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(CURRENT_HUNTING_YEAR);

        assertEquals("both hunting years are included",
                ImmutableSet.of(CURRENT_HUNTING_YEAR, NEXT_HUNTING_YEAR), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_CurrentYearReference() {
        Person p = new Person();
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE1);
        p.setInvoiceReferencePreviousYear(CURRENT_HUNTING_YEAR);

        assertEquals("current year reference is included",
                ImmutableSet.of(CURRENT_HUNTING_YEAR), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NoPayments_OldYearReference() {
        Person p = new Person();
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE1);
        p.setInvoiceReferencePreviousYear(CURRENT_HUNTING_YEAR - 1);

        assertEquals("old reference is ignored", ImmutableSet.of(), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_OldPayment_BothYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(NEXT_HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(CURRENT_HUNTING_YEAR);
        p.setHuntingPaymentOneYear(2012);
        p.setHuntingPaymentOneDay(ld(2012, 6, 9));
        p.setHuntingPaymentTwoYear(2013);
        p.setHuntingPaymentTwoDay(ld(2013, 6, 9));

        assertEquals("both hunting years are included",
                ImmutableSet.of(CURRENT_HUNTING_YEAR, NEXT_HUNTING_YEAR), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_CurrentPayment_BothYearReference() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(NEXT_HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(CURRENT_HUNTING_YEAR);
        p.setHuntingPaymentOneYear(CURRENT_HUNTING_YEAR);
        p.setHuntingPaymentOneDay(ld(CURRENT_HUNTING_YEAR, 6, 9));

        assertEquals("both hunting years are included",
                ImmutableSet.of(NEXT_HUNTING_YEAR), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_AllPayments_BothReferences() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(NEXT_HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(CURRENT_HUNTING_YEAR);
        p.setHuntingPaymentOneYear(NEXT_HUNTING_YEAR);
        p.setHuntingPaymentOneDay(ld(NEXT_HUNTING_YEAR, 6, 9));
        p.setHuntingPaymentTwoYear(CURRENT_HUNTING_YEAR);
        p.setHuntingPaymentTwoDay(ld(CURRENT_HUNTING_YEAR, 6, 9));

        assertEquals("both hunting years are excluded",
                ImmutableSet.of(), getHuntingPaymentPdfYear(p));
    }

    @Test
    public void testHuntingPaymentPdfYears_NotBeforeMagazineReleased() {
        Person p = new Person();
        p.setInvoiceReferenceCurrent(INVOICE_REFERENCE1);
        p.setInvoiceReferenceCurrentYear(CURRENT_HUNTING_YEAR);
        p.setInvoiceReferencePrevious(INVOICE_REFERENCE2);
        p.setInvoiceReferencePreviousYear(NEXT_HUNTING_YEAR);

        assertEquals("only current",
                ImmutableSet.of(CURRENT_HUNTING_YEAR), HuntingPaymentUtil.getHuntingPaymentPdfYears(
                        new LocalDate(2017, 7, 14), p));

        assertEquals("both",
                ImmutableSet.of(CURRENT_HUNTING_YEAR, NEXT_HUNTING_YEAR), HuntingPaymentUtil.getHuntingPaymentPdfYears(
                        new LocalDate(2017, 7, 15), p));

        assertEquals("both",
                ImmutableSet.of(CURRENT_HUNTING_YEAR, NEXT_HUNTING_YEAR), HuntingPaymentUtil.getHuntingPaymentPdfYears(
                        new LocalDate(2017, 7, 31), p));

        assertEquals("only next",
                ImmutableSet.of(NEXT_HUNTING_YEAR), HuntingPaymentUtil.getHuntingPaymentPdfYears(
                        new LocalDate(2017, 8, 1), p));
    }
}
