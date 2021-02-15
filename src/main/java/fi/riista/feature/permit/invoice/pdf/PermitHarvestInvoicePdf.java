package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.util.ContentDispositionUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nonnull;
import java.io.IOException;

import static fi.riista.feature.common.money.FinnishBankAccount.MOOSELIKE_HARVEST_FEE_OP_POHJOLA;
import static java.util.Objects.requireNonNull;

public class PermitHarvestInvoicePdf {

    public static PermitHarvestInvoicePdf createBlankInvoice(final @Nonnull PermitDecision decision,
                                                             final @Nonnull GameSpecies gameSpecies) throws IOException {

        return create(PermitHarvestInvoicePdfModel.createBlank(
                decision, MOOSELIKE_HARVEST_FEE_OP_POHJOLA, gameSpecies));
    }

    public static PermitHarvestInvoicePdf createInvoice(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                        final @Nonnull Invoice invoice) throws IOException {

        return create(PermitHarvestInvoicePdfModel.createInvoice(speciesAmount, invoice));
    }

    public static PermitHarvestInvoicePdf createReceipt(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                        final @Nonnull Invoice invoice) throws IOException {

        return create(PermitHarvestInvoicePdfModel.createReceipt(speciesAmount, invoice));
    }

    public static PermitHarvestInvoicePdf createReminder(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                         final @Nonnull Invoice invoice) throws IOException {

        return create(PermitHarvestInvoicePdfModel.createReminder(speciesAmount, invoice));
    }

    private static PermitHarvestInvoicePdf create(final @Nonnull PermitHarvestInvoicePdfModel model) throws IOException {
        final byte[] data = PermitHarvestInvoicePdfBuilder.getPdf(model);
        return new PermitHarvestInvoicePdf(data, model.getPdfFileName());
    }

    private final byte[] data;
    private final String fileName;

    private PermitHarvestInvoicePdf(final byte[] data, final String fileName) {
        this.data = requireNonNull(data);
        this.fileName = requireNonNull(fileName);
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public ResponseEntity<byte[]> asResponseEntity() {
        return ResponseEntity.ok()
                .headers(ContentDispositionUtil.header(fileName))
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .body(data);
    }
}
