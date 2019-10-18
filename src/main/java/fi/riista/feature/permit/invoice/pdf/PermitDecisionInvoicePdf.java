package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.util.ContentDispositionUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nonnull;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class PermitDecisionInvoicePdf {

    public static PermitDecisionInvoicePdf createInvoice(final @Nonnull PermitDecision decision,
                                                         final @Nonnull Invoice invoice) throws IOException {
        return create(PermitDecisionInvoicePdfModel.ResultType.INVOICE, decision, invoice);
    }

    public static PermitDecisionInvoicePdf createReceipt(final @Nonnull PermitDecision decision,
                                                         final @Nonnull Invoice invoice) throws IOException {
        return create(PermitDecisionInvoicePdfModel.ResultType.RECEIPT, decision, invoice);
    }

    public static PermitDecisionInvoicePdf createReminder(final @Nonnull PermitDecision decision,
                                                          final @Nonnull Invoice invoice) throws IOException {
        return create(PermitDecisionInvoicePdfModel.ResultType.REMINDER, decision, invoice);
    }

    private static PermitDecisionInvoicePdf create(final @Nonnull PermitDecisionInvoicePdfModel.ResultType resultType,
                                                   final @Nonnull PermitDecision decision,
                                                   final @Nonnull Invoice invoice) throws IOException {
        final PermitDecisionInvoicePdfModel model = PermitDecisionInvoicePdfModel.create(resultType, decision, invoice);
        final byte[] data = PermitDecisionInvoicePdfBuilder.getPdf(model);

        return new PermitDecisionInvoicePdf(data, model);
    }

    private final byte[] data;
    private final String fileName;

    private PermitDecisionInvoicePdf(final byte[] data, final PermitDecisionInvoicePdfModel model) {
        this(data, model.getPdfFileName());
    }

    private PermitDecisionInvoicePdf(final byte[] data, final String fileName) {
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
