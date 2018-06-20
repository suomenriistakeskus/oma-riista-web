package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.InvoiceAccountDetails;
import fi.riista.util.ContentDispositionUtil;
import org.iban4j.Bic;
import org.iban4j.Iban;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nonnull;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class PermitHarvestInvoicePdf {

    private static final InvoiceAccountDetails HARVEST_BANK_ACCOUNT =
            new InvoiceAccountDetails(Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI2950000121502875"));

    static PermitHarvestInvoicePdf createInvoice(final @Nonnull PermitDecision decision,
                                                 final @Nonnull GameSpecies gameSpecies) throws IOException {
        final PermitHarvestInvoicePdfModel model =
                PermitHarvestInvoicePdfModel.create(decision, HARVEST_BANK_ACCOUNT, gameSpecies);
        final byte[] data = PermitHarvestInvoicePdfBuilder.getPdf(model);

        return new PermitHarvestInvoicePdf(data, model);
    }

    private final byte[] data;
    private final String fileName;

    private PermitHarvestInvoicePdf(final byte[] data, final PermitHarvestInvoicePdfModel model) {
        this(data, model.getPdfFileName());
    }

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
