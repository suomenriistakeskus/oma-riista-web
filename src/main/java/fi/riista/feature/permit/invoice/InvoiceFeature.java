package fi.riista.feature.permit.invoice;

import fi.riista.feature.RequireEntityService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Service
public class InvoiceFeature {

    @Resource
    private PermitInvoiceService permitInvoiceService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private InvoiceDTOTransformer dtoTransformer;

    @Transactional(readOnly = true)
    public InvoiceDTO getInvoice(final long id) {
        final Invoice invoice = requireEntityService.requireInvoice(id, READ);
        return dtoTransformer.apply(invoice);
    }

    @Transactional
    public InvoiceDTO disableElectronicInvoicing(final long invoiceId) {
        final Invoice invoice = requireEntityService.requireInvoice(invoiceId, UPDATE);
        permitInvoiceService.disableElectronicInvoicing(invoice);
        return dtoTransformer.apply(invoice);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getInvoicePdfFile(final long invoiceId) throws IOException {
        final Invoice invoice = requireEntityService.requireInvoice(invoiceId, READ);
        return permitInvoiceService.getInvoicePdfFile(invoice);
    }

    @Transactional(rollbackFor = IOException.class)
    public ResponseEntity<byte[]> createInvoiceReminderPdfFile(final long invoiceId) throws IOException {
        final Invoice invoice = requireEntityService.requireInvoice(invoiceId, UPDATE);
        return permitInvoiceService.getReminderPdfFileAndUpdateInvoiceState(invoice);
    }
}
