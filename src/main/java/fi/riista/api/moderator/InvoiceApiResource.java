package fi.riista.api.moderator;

import fi.riista.feature.permit.invoice.payment.AddInvoicePaymentLineDTO;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLineFeature;
import fi.riista.feature.permit.invoice.search.InvoiceModeratorDTO;
import fi.riista.feature.permit.invoice.search.InvoiceModeratorFeature;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFeature;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO;
import fi.riista.feature.permit.invoice.search.InvoiceSearchResultDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private InvoiceSearchFeature invoiceSearchFeature;

    @Resource
    private InvoiceModeratorFeature invoiceModeratorFeature;

    @Resource
    private InvoicePaymentLineFeature invoicePaymentLineFeature;

    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    public List<InvoiceSearchResultDTO> searchInvoices(@Valid @RequestBody final InvoiceSearchFilterDTO dto) {
        return invoiceSearchFeature.searchInvoices(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{invoiceId:\\d+}", produces = APPLICATION_JSON_UTF8_VALUE)
    public InvoiceModeratorDTO getInvoice(@PathVariable final long invoiceId) {
        return invoiceModeratorFeature.getInvoice(invoiceId);
    }

    @PutMapping(value = "/{invoiceId:\\d+}/disableelectronicinvoicing", produces = APPLICATION_JSON_UTF8_VALUE)
    public InvoiceModeratorDTO disableElectronicInvoicing(@PathVariable final long invoiceId) {
        return invoiceModeratorFeature.disableElectronicInvoicing(invoiceId);
    }

    @PostMapping(value = "/{invoiceId:\\d+}/paymentline", produces = APPLICATION_JSON_UTF8_VALUE)
    public InvoiceModeratorDTO addInvoicePaymentLine(@PathVariable final long invoiceId,
                                                     @Valid @RequestBody final AddInvoicePaymentLineDTO dto) {

        return invoicePaymentLineFeature.addInvoicePaymentLine(invoiceId, dto);
    }

    @DeleteMapping(value = "/paymentline/{invoicePaymentLineId:\\d+}", produces = APPLICATION_JSON_UTF8_VALUE)
    public InvoiceModeratorDTO removePaymentLine(@PathVariable final long invoicePaymentLineId) {
        return invoicePaymentLineFeature.removeInvoicePaymentLine(invoicePaymentLineId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{invoiceId:\\d+}/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProcessingInvoice(final @PathVariable long invoiceId) throws IOException {
        return invoiceModeratorFeature.getInvoicePdfFile(invoiceId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{invoiceId:\\d+}/reminder/pdf", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProcessingInvoiceReminder(final @PathVariable long invoiceId) throws IOException {
        return invoiceModeratorFeature.createInvoiceReminder(invoiceId);
    }
}
