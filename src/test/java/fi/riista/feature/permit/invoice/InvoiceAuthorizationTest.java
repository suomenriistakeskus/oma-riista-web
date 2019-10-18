package fi.riista.feature.permit.invoice;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import static fi.riista.feature.account.user.SystemUserPrivilege.ALTER_INVOICE_PAYMENT;
import static fi.riista.feature.permit.invoice.InvoiceAuthorization.InvoicePermission.CREATE_REMOVE_PAYMENT_LINES_MANUALLY;

public class InvoiceAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void testAddRemovePaymentLines_admin() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            testPermission(invoice, CREATE_REMOVE_PAYMENT_LINES_MANUALLY, true);
        });
    }

    @Test
    public void testAddRemovePaymentLines_moderator() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            testPermission(invoice, CREATE_REMOVE_PAYMENT_LINES_MANUALLY, false);
        });
    }

    @Test
    public void testAddRemovePaymentLines_privilegedModerator() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {
            testPermission(invoice, CREATE_REMOVE_PAYMENT_LINES_MANUALLY, true);
        });
    }

    private void testPermission(final Invoice invoice, final Enum<?> permission, final boolean permitted) {
        onCheckingPermission(permission)
                .expect(permitted)
                .expectNumberOfQueriesAtMost(0)
                .apply(invoice);
    }
}
