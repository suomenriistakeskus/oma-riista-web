package fi.riista.feature.permit.invoice.pdf;

import com.google.common.io.Files;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.Locales;
import org.junit.Test;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class PermitHarvestInvoicePdfTest {

    @Test
    public void testCreateBlankInvoice() throws IOException {
        createDummyPdf();
    }

    private static PermitHarvestInvoicePdf createDummyPdf() throws IOException {
        final GameSpecies gameSpecies = InvoicePdfTestData.createMoose();
        final PermitDecision decision = InvoicePdfTestData.createDecision(Locales.SV);

        return PermitHarvestInvoicePdf.createBlankInvoice(decision, gameSpecies);
    }

    public static void main(final String[] args) {
        try {
            final byte[] data = createDummyPdf().getData();
            final File tempFile = File.createTempFile("harvest-invoice", ".pdf");
            Files.write(data, tempFile);

            if (Desktop.isDesktopSupported()) {
                final Desktop desktop = Desktop.getDesktop();
                desktop.browse(tempFile.toURI());
            }

        } catch (final IOException e) {
            throw new RuntimeException("Could not generate PDF", e);
        }
    }
}
