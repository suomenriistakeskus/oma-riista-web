package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEvent;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEventRepository;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoiceRepository;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoice;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceRepository;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdf;
import fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdf;
import fi.riista.feature.storage.FileDownloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

import static fi.riista.feature.permit.invoice.InvoiceStateChangeEventType.ELECTRONIC_INVOICING_DISABLED;
import static fi.riista.feature.permit.invoice.InvoiceStateChangeEventType.OVERDUE_REMINDER_CREATED;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class InvoiceModeratorFeature {

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private PermitHarvestInvoiceRepository permitHarvestInvoiceRepository;

    @Resource
    private InvoiceStateChangeEventRepository invoiceEventRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private InvoiceModeratorDTOTransformer dtoTransformer;

    @Transactional(readOnly = true)
    public InvoiceModeratorDTO getInvoice(final long id) {
        return dtoTransformer.apply(requireEntityService.requireInvoice(id, READ));
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getInvoicePdfFile(final long invoiceId) throws IOException {
        return fileDownloadService.download(requireEntityService.requireInvoice(invoiceId, READ).getPdfFileMetadata());
    }

    @Transactional
    public InvoiceModeratorDTO disableElectronicInvoicing(final long invoiceId) {
        final Invoice invoice = requireEntityService.requireInvoice(invoiceId, UPDATE);
        invoice.disableElectronicInvoicing();

        invoiceEventRepository.save(new InvoiceStateChangeEvent(invoice, ELECTRONIC_INVOICING_DISABLED));

        return dtoTransformer.apply(invoice);
    }

    @Transactional(rollbackFor = IOException.class)
    public ResponseEntity<byte[]> createInvoiceReminder(final long invoiceId) throws IOException {
        final Invoice invoice = requireEntityService.requireInvoice(invoiceId, UPDATE);

        invoice.setStateReminder();

        invoiceEventRepository.save(new InvoiceStateChangeEvent(invoice, OVERDUE_REMINDER_CREATED));

        return createReminder(invoice);
    }

    private ResponseEntity<byte[]> createReminder(final Invoice invoice) throws IOException {
        switch (invoice.getType()) {
            case PERMIT_PROCESSING:
                final PermitDecision decision = permitDecisionInvoiceRepository
                        .findByInvoice(invoice)
                        .map(PermitDecisionInvoice::getDecision)
                        .orElseThrow(() -> new IllegalArgumentException("Could not find decision for invoice id=" + invoice.getId()));
                return PermitDecisionInvoicePdf.createReminder(decision, invoice).asResponseEntity();

            case PERMIT_HARVEST:
                final HarvestPermitSpeciesAmount speciesAmount = permitHarvestInvoiceRepository
                        .findByInvoice(invoice)
                        .map(PermitHarvestInvoice::getSpeciesAmount)
                        .orElseThrow(() -> new IllegalArgumentException("Could not find speciesAmount for invoice id=" + invoice.getId()));
                return PermitHarvestInvoicePdf.createReminder(speciesAmount, invoice).asResponseEntity();

            default:
                throw new IllegalArgumentException("Not supported");
        }
    }
}
