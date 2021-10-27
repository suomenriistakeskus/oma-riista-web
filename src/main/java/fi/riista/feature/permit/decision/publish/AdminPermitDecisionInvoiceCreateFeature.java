package fi.riista.feature.permit.decision.publish;

import com.mchange.v2.collection.MapEntry;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdf;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.MediaTypeExtras;
import io.vavr.Tuple2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminPermitDecisionInvoiceCreateFeature {

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private FileStorageService fileStorageService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void createInvoicePdfs(final List<Long> invoiceIds) throws IOException {
        final List<Invoice> invoices = invoiceRepository.findAllById(invoiceIds);

        final Map<Invoice, PermitDecision> invoicesAndDecisions =
                permitDecisionRepository.findByInvoiceIn(invoices);

        for (final Invoice invoice : invoices) {
            final PermitDecision decision = invoicesAndDecisions.get(invoice);
            invoice.setPdfFileMetadata(storeInvoiceFile(PermitDecisionInvoicePdf.createInvoice(decision, invoice)));
            invoiceRepository.save(invoice);
        }
    }

    private PersistentFileMetadata storeInvoiceFile(final PermitDecisionInvoicePdf invoicePdf) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(), invoicePdf.getData(), FileType.INVOICE_PDF,
                MediaTypeExtras.APPLICATION_PDF_VALUE, invoicePdf.getFileName());
    }
}
