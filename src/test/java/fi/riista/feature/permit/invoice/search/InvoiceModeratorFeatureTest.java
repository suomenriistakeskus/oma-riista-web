package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEvent;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEventRepository;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEventType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class InvoiceModeratorFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private InvoiceModeratorFeature feature;

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private InvoiceStateChangeEventRepository invoiceEventRepository;

    @Test
    public void testDisableElectronicInvoicing_forProcessingInvoice() {
        final Invoice invoice = model().newPermitDecisionInvoice().getInvoice();
        testDisableElectronicInvoicing(invoice);
    }

    @Test
    public void testDisableElectronicInvoicing_forHarvestInvoice() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();
        testDisableElectronicInvoicing(invoice);
    }

    private void testDisableElectronicInvoicing(final Invoice invoice) {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            try {
                final long invoiceId = invoice.getId();
                feature.disableElectronicInvoicing(invoiceId);

                runInTransaction(() -> {
                    final Invoice refreshed = invoiceRepository.findById(invoiceId).orElse(null);
                    assertFalse(refreshed.isElectronicInvoicingEnabled());

                    assertInvoiceEventCreated(invoice, InvoiceStateChangeEventType.ELECTRONIC_INVOICING_DISABLED);
                });

            } catch (final Exception e) {
                fail("Failed with exception " + e);
            }
        });
    }

    @Test
    public void testCreateInvoiceReminder_smokeTestForProcessingInvoice() {
        final Invoice invoice = model().newPermitDecisionInvoice().getInvoice();
        testCreateInvoiceReminder(invoice);
    }

    @Test
    public void testCreateInvoiceReminder_smokeTestForHarvestInvoice() {
        final Invoice invoice = model().newPermitHarvestInvoice(model().newGameSpeciesMoose()).getInvoice();

        testCreateInvoiceReminder(invoice);
    }

    private void testCreateInvoiceReminder(final Invoice invoice) {
        invoice.disableElectronicInvoicing();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            try {
                final long invoiceId = invoice.getId();
                feature.createInvoiceReminder(invoiceId);

                runInTransaction(() -> {
                    final Invoice refreshed = invoiceRepository.findById(invoiceId).orElse(null);
                    assertEquals(InvoiceState.REMINDER, refreshed.getState());

                    assertInvoiceEventCreated(refreshed, InvoiceStateChangeEventType.OVERDUE_REMINDER_CREATED);
                });

            } catch (final Exception e) {
                fail("Failed with exception " + e);
            }
        });
    }

    private void assertInvoiceEventCreated(final Invoice invoice, final InvoiceStateChangeEventType expectedType) {
        final List<InvoiceStateChangeEvent> events = invoiceEventRepository.findAll();
        assertEquals(1, events.size());

        final InvoiceStateChangeEvent event = events.get(0);
        assertEquals(invoice, event.getInvoice());
        assertEquals(expectedType, event.getType());
    }
}
