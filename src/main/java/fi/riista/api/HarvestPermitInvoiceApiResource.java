package fi.riista.api;

import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.PermitInvoiceListDTO;
import fi.riista.feature.permit.invoice.PermitInvoiceListFeature;
import fi.riista.feature.permit.invoice.PermitInvoicePaymentFeature;
import fi.riista.util.MediaTypeExtras;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/harvestpermit/{permitId:\\d+}/invoice")
public class HarvestPermitInvoiceApiResource {

    @Resource
    private PermitInvoiceListFeature invoiceListFeature;

    @Resource
    private PermitInvoicePaymentFeature invoicePaymentFeature;

    @GetMapping(value = "/due", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitInvoiceListDTO> listDueByPermit(final @PathVariable long permitId) {
        return invoiceListFeature.listByPermit(permitId, InvoiceState.DELIVERED);
    }

    @GetMapping(value = "/paid", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitInvoiceListDTO> listPaidByPermit(final @PathVariable long permitId) {
        return invoiceListFeature.listByPermit(permitId, InvoiceState.PAID);
    }

    @GetMapping(value = "/{invoiceId:\\d+}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitInvoiceListDTO getInvoice(final @PathVariable long permitId,
                                           final @PathVariable long invoiceId) {
        return invoiceListFeature.getPermitInvoice(permitId, invoiceId);
    }

    @PostMapping(value = "/{invoiceId:\\d+}/receipt", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPaymentReceipt(final @PathVariable long permitId,
                                                    final @PathVariable long invoiceId) throws IOException {
        return invoiceListFeature.getPaymentReceiptPdfFile(permitId, invoiceId);
    }

    @PostMapping(value = "/{invoiceId:\\d+}/payment",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, String> getPaymentForm(final @PathVariable long permitId,
                                              final @PathVariable long invoiceId) {
        return invoicePaymentFeature.getPaymentForm(permitId, invoiceId);
    }
}
