package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.money.BankAccount;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.util.BigDecimalMoney;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import static fi.riista.feature.permit.invoice.CreditorReferenceCalculator.computeReferenceForPermitHarvestInvoice;
import static fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdfModel.DUE_DATE_PATTERN;
import static fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdfModel.INVOICE_DATE_PATTERN;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PermitHarvestInvoicePdfModelTest {

    @Test
    public void testCreateBlankInvoice_fi() {
        testCreateBlankInvoice(Locales.FI);
    }

    @Test
    public void testCreateBlankInvoice_sv() {
        testCreateBlankInvoice(Locales.SV);
    }

    private static void testCreateBlankInvoice(final Locale locale) {
        final GameSpecies moose = InvoicePdfTestData.createMoose();
        final PermitDecision decision = InvoicePdfTestData.createDecision(locale);
        final FinnishBankAccount invoiceAccount = FinnishBankAccount.MOOSELIKE_HARVEST_FEE_OP_POHJOLA;

        final PermitHarvestInvoicePdfModel model =
                PermitHarvestInvoicePdfModel.createBlank(decision, invoiceAccount, moose);

        assertCommonPartsOfInvoicePdf(model, decision);
        assertAccountDetails(model, invoiceAccount);

        final CreditorReference expectedReference = computeReferenceForPermitHarvestInvoice(
                decision.getDecisionYear(), decision.getDecisionNumber(), moose.getOfficialCode());
        assertEquals(expectedReference.toString(), model.getInvoiceReferenceForHuman());

        assertEquals(INVOICE_DATE_PATTERN.print(decision.getPublishDate()), model.getInvoiceDateString());
        assertNull(model.getDueDate());
        assertEquals("", model.getDueDateString());
        assertEquals("", model.getPaymentDateString());
        assertEquals("", model.getInvoiceAmountText());
        assertTrue(model.includePaymentTable());
    }

    @Test
    public void testCreateInvoice_fi() {
        testCreateInvoice(Locales.FI);
    }

    @Test
    public void testCreateInvoice_sv() {
        testCreateInvoice(Locales.SV);
    }

    private static void testCreateInvoice(final Locale locale) {
        final GameSpecies moose = InvoicePdfTestData.createMoose();
        final PermitDecision decision = InvoicePdfTestData.createDecision(locale);
        final HarvestPermitSpeciesAmount speciesAmount = InvoicePdfTestData.createSpeciesAmount(decision, moose);
        final Invoice invoice = InvoicePdfTestData.createHarvestInvoice(speciesAmount);

        final PermitHarvestInvoicePdfModel model = PermitHarvestInvoicePdfModel.createInvoice(speciesAmount, invoice);

        assertCommonPartsOfInvoicePdf(model, decision);
        assertCommonPaymentTerms(model, invoice);

        assertEquals(INVOICE_DATE_PATTERN.print(invoice.getInvoiceDate()), model.getInvoiceDateString());
        assertEquals(DUE_DATE_PATTERN.print(invoice.getDueDate()), model.getDueDateString());
        assertEquals("", model.getPaymentDateString());
        assertEquals(new BigDecimalMoney(invoice.getAmount()).formatPaymentAmount(), model.getInvoiceAmountText());
    }

    private static void assertCommonPartsOfInvoicePdf(final PermitHarvestInvoicePdfModel model,
                                                      final PermitDecision decision) {

        assertCommonParts(model, decision);
        assertEquals(Locales.isSwedish(decision.getLocale()) ? "FAKTURA" : "LASKU", model.getInvoiceTitle());
    }

    @Test
    public void testCreateReminder_whenNoPaymentReceived_fi() {
        testCreateReminder(Locales.FI, 120, null, null, 120);
    }

    @Test
    public void testCreateReminder_whenNoPaymentReceived_sv() {
        testCreateReminder(Locales.SV, 120, null, null, 120);
    }

    @Test
    public void testCreateReminder_whenPartialPaymentReceived() {
        testCreateReminder(Locales.FI, 120, null, 80, 40);
    }

    @Test
    public void testCreateReminder_whenFullPaymentReceived() {
        testCreateReminder(Locales.FI, 120, null, 120, 0);
    }

    @Test
    public void testCreateReminder_whenInvoiceCorrectedButNoPaymentReceived() {
        testCreateReminder(Locales.FI, 120, 240, null, 240);
    }

    @Test
    public void testCreateReminder_whenInvoiceCorrectedAndPartialPaymentReceived() {
        testCreateReminder(Locales.FI, 120, 240, 120, 120);
    }

    @Test
    public void testCreateReminder_whenInvoiceCorrectedAndFullPaymentReceived() {
        testCreateReminder(Locales.FI, 120, 240, 240, 0);
    }

    private static void testCreateReminder(final Locale locale,
                                           final int amount,
                                           final Integer correctedAmount,
                                           final Integer receivedAmount,
                                           final int expectedResultAmount) {

        final GameSpecies moose = InvoicePdfTestData.createMoose();
        final PermitDecision decision = InvoicePdfTestData.createDecision(locale);
        final HarvestPermitSpeciesAmount speciesAmount = InvoicePdfTestData.createSpeciesAmount(decision, moose);

        final Invoice invoice = InvoicePdfTestData.createHarvestInvoice(speciesAmount);
        invoice.setAmount(new BigDecimal(amount));
        Optional.ofNullable(correctedAmount).map(BigDecimal::new).ifPresent(invoice::setCorrectedAmount);
        Optional.ofNullable(receivedAmount).map(BigDecimal::new).ifPresent(invoice::setReceivedAmount);

        final PermitHarvestInvoicePdfModel model = PermitHarvestInvoicePdfModel.createReminder(speciesAmount, invoice);

        assertCommonParts(model, decision);
        assertCommonPaymentTerms(model, invoice);

        assertEquals(INVOICE_DATE_PATTERN.print(today()), model.getInvoiceDateString());
        assertEquals("", model.getPaymentDateString());
        assertEquals(new BigDecimalMoney(expectedResultAmount, 0).formatPaymentAmount(), model.getInvoiceAmountText());

        if (Locales.isSwedish(locale)) {
            assertEquals("BETALNINGSPÅMINNELSE", model.getInvoiceTitle());
            assertEquals("OMEDELBART", model.getDueDateString());
        } else {
            assertEquals("MAKSUMUISTUTUS", model.getInvoiceTitle());
            assertEquals("HETI", model.getDueDateString());
        }
    }

    @Test
    public void testCreateReceipt_whenAccountStatementForPaytrailPaymentNotYetReceived_fi() {
        testCreateReceipt(Locales.FI, true, null, 120);
    }

    @Test
    public void testCreateReceipt_whenAccountStatementForPaytrailPaymentNotYetReceived_sv() {
        testCreateReceipt(Locales.SV, true, null, 120);
    }

    @Test
    public void testCreateReceipt_whenAccountStatementForPaytrailPaymentReceived() {
        testCreateReceipt(Locales.FI, true, 120, 120);
    }

    @Test
    public void testCreateReceipt_whenAccountStatementForFullOfflinePaymentReceived() {
        testCreateReceipt(Locales.FI, false, 120, 120);
    }

    @Test
    public void testCreateReceipt_whenAccountStatementForPartialOfflinePaymentReceived() {
        testCreateReceipt(Locales.FI, false, 50, 50);
    }

    @Test
    public void testCreateReceipt_whenAccountStatementForOfflinePaymentNotYetReceived() {
        testCreateReceipt(Locales.FI, false, null, 0);
    }

    // When permit holder made offline payment in addition to having done payment via Paytrail.
    @Test
    public void testCreateReceipt_whenPaymentGreaterThanInvoicedAmount() {
        testCreateReceipt(Locales.FI, true, 240, 240);
    }

    @Test
    public void testCreateReceipt_whenSumOfOfflinePaymentsGreaterThanInvoicedAmount() {
        testCreateReceipt(Locales.FI, false, 240, 240);
    }

    private static void testCreateReceipt(final Locale locale,
                                          final boolean paymentMadeViaPaytrail,
                                          final Integer receivedAmount,
                                          final int expectedResultAmount) {

        final GameSpecies moose = InvoicePdfTestData.createMoose();
        final PermitDecision decision = InvoicePdfTestData.createDecision(locale);
        final HarvestPermitSpeciesAmount speciesAmount = InvoicePdfTestData.createSpeciesAmount(decision, moose);

        final Invoice invoice = InvoicePdfTestData.createHarvestInvoice(speciesAmount);
        final LocalDate paymentDate = today().minusDays(1);
        invoice.setPaid(paymentDate);

        if (paymentMadeViaPaytrail) {
            invoice.setPaytrailPaymentId("1");
        }
        Optional.ofNullable(receivedAmount).map(BigDecimal::new).ifPresent(invoice::setReceivedAmount);

        final PermitHarvestInvoicePdfModel model = PermitHarvestInvoicePdfModel.createReceipt(speciesAmount, invoice);

        assertCommonParts(model, decision);
        assertCommonPaymentTerms(model, invoice);

        assertEquals(INVOICE_DATE_PATTERN.print(paymentDate), model.getInvoiceDateString());
        assertEquals(INVOICE_DATE_PATTERN.print(paymentDate), model.getPaymentDateString());
        assertEquals(new BigDecimalMoney(expectedResultAmount, 0).formatPaymentAmount(), model.getInvoiceAmountText());

        if (Locales.isSwedish(locale)) {
            assertEquals("KVITTO", model.getInvoiceTitle());
            assertEquals("BETALD", model.getDueDateString());
        } else {
            assertEquals("KUITTI", model.getInvoiceTitle());
            assertEquals("MAKSETTU", model.getDueDateString());
        }
    }

    private static void assertCommonParts(final PermitHarvestInvoicePdfModel model, final PermitDecision decision) {
        assertEquals(PermitHarvestInvoicePdfModel.PAYMENT_RECIPIENT, model.getPaymentRecipient());

        assertEquals(decision.createPermitNumber(), model.getPermitNumber());
        assertEquals("120 €", model.getSpecimenPrice().formatAdultPrice());
        assertEquals(" 50 €", model.getSpecimenPrice().formatYoungPrice());

        assertInvoiceRecipient(model, decision.getContactPerson());
    }

    private static void assertInvoiceRecipient(final PermitHarvestInvoicePdfModel model, final Person contactPerson) {
        requireNonNull(contactPerson);

        final InvoicePdfRecipient recipient = model.getInvoiceRecipient();

        assertEquals(Long.toString(contactPerson.getId()), recipient.getCustomerNumber());

        final Address address = requireNonNull(contactPerson.getAddress());
        assertEquals(address.getStreetAddress(), recipient.getStreetAddress());
        assertEquals(address.getPostalCode(), recipient.getPostalCode());
        assertEquals(address.getCity(), recipient.getCity());
    }

    private static void assertCommonPaymentTerms(final PermitHarvestInvoicePdfModel model, final Invoice invoice) {
        assertAccountDetails(model, invoice.resolveBankAccountDetails());
        assertEquals(invoice.getCreditorReference().toString(), model.getInvoiceReferenceForHuman());
        assertEquals(invoice.getDueDate(), model.getDueDate());
        assertFalse(model.includePaymentTable());
    }

    private static void assertAccountDetails(final PermitHarvestInvoicePdfModel model, final BankAccount expected) {
        assertEquals(expected.getBic(), model.getInvoiceAccountDetails().getBic());
        assertEquals(expected.getIban(), model.getInvoiceAccountDetails().getIban());
        assertEquals(expected.getBankName(), model.getInvoiceAccountDetails().getBankName());
    }
}
