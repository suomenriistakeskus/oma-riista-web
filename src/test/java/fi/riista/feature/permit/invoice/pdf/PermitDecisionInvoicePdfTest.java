package fi.riista.feature.permit.invoice.pdf;

import com.google.common.io.Files;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.util.Locales;
import org.junit.Test;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class PermitDecisionInvoicePdfTest {

    @Test
    public void testGetPdf() throws IOException {
        createDummyPdf();
    }

    private static PermitDecisionInvoicePdf createDummyPdf() throws IOException {
        final PermitDecision dummyDecision = InvoicePdfTestData.createDecision(Locales.SV);
        final Invoice invoice = InvoicePdfTestData.createProcessingInvoice(dummyDecision);

        return PermitDecisionInvoicePdf.createInvoice(dummyDecision, invoice);
    }

    public static void main(final String[] args) {
        try {
            final byte[] pdfData = createDummyPdf().getData();
            final File tempFile = File.createTempFile("decision-invoice", ".pdf");
            Files.write(pdfData, tempFile);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
            }

        } catch (final IOException e) {
            throw new RuntimeException("Could not generate PDF", e);
        }
    }
}
