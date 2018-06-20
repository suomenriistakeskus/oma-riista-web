package fi.riista.api;

import fi.riista.feature.permit.invoice.InvoiceDTO;
import fi.riista.feature.permit.invoice.InvoiceFeature;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFeature;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO;
import fi.riista.feature.permit.invoice.search.InvoiceSearchResultDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1/invoice")
public class InvoiceApiResource {

    @Resource
    private InvoiceFeature invoiceFeature;

    @Resource
    private InvoiceSearchFeature invoiceSearchFeature;

    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    public List<InvoiceSearchResultDTO> searchInvoices(@Valid @RequestBody final InvoiceSearchFilterDTO dto) {
        return invoiceSearchFeature.searchInvoices(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{invoiceId:\\d+}", produces = APPLICATION_JSON_UTF8_VALUE)
    public InvoiceDTO getInvoice(@PathVariable final long invoiceId) {
        return invoiceFeature.getInvoice(invoiceId);
    }

    @PostMapping(value = "/{invoiceId:\\d+}/disableelectronicinvoicing", produces = APPLICATION_JSON_UTF8_VALUE)
    public InvoiceDTO disableElectronicInvoicing(@PathVariable final long invoiceId) {
        return invoiceFeature.disableElectronicInvoicing(invoiceId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{invoiceId:\\d+}/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProcessingInvoice(final @PathVariable long invoiceId) throws IOException {
        return invoiceFeature.getInvoicePdfFile(invoiceId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{invoiceId:\\d+}/reminder/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProcessingInvoiceReminder(final @PathVariable long invoiceId) throws IOException {
        return invoiceFeature.createInvoiceReminderPdfFile(invoiceId);
    }
}
