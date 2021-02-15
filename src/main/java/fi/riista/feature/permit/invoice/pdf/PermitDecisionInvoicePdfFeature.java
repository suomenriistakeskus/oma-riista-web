package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoiceRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.EnumSet;

@Service
public class PermitDecisionInvoicePdfFeature {

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getProcessingInvoicePdfFile(final long decisionId) throws IOException {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        decision.assertStatus(EnumSet.of(DecisionStatus.PUBLISHED));

        final PermitDecisionInvoice permitDecisionInvoice = permitDecisionInvoiceRepository.getByDecision(decision);
        final Invoice invoice = permitDecisionInvoice.getInvoice();

        if (invoice.isElectronicInvoicingEnabled()) {
            throw new IllegalStateException(String.format(
                    "Refusing to download invoice PDF for decisionId %d having electronic invoicing enabled", decisionId));
        }

        return fileDownloadService.download(invoice.getPdfFileMetadata());
    }
}
