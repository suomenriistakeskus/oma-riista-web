package fi.riista.feature.permit.invoice;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.pdf.InvoicePdfConstants;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdf;
import fi.riista.security.EntityPermission;
import fi.riista.util.BigDecimalMoney;
import fi.riista.util.Locales;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Component
public class PermitInvoiceListFeature {

    private static final InvoiceContactDetailsDTO RK_FI = InvoiceContactDetailsDTO.create(
            InvoicePdfConstants.RK_NAME.getFinnish(),
            InvoicePdfConstants.RK_STREET.getFinnish(),
            InvoicePdfConstants.RK_POST_OFFICE.getFinnish(),
            InvoicePdfConstants.RK_PHONE);

    private static final InvoiceContactDetailsDTO RK_SV = InvoiceContactDetailsDTO.create(
            InvoicePdfConstants.RK_NAME.getSwedish(),
            InvoicePdfConstants.RK_STREET.getSwedish(),
            InvoicePdfConstants.RK_POST_OFFICE.getSwedish(),
            InvoicePdfConstants.RK_PHONE);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitInvoiceService permitInvoiceService;

    @Resource
    private InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public PermitInvoiceListDTO getPermitInvoice(final long permitId,
                                                 final long invoiceId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final PermitDecision permitDecision = harvestPermit.getPermitDecision();

        if (permitDecision == null) {
            return null;
        }

        final Invoice invoice = invoiceRepository.getInvoice(permitDecision, invoiceId);

        if (invoice == null) {
            throw new NotFoundException();
        }

        final String permitNumber = permitDecision.getApplication().getPermitNumber();
        final Locale locale = permitDecision.getLocale();

        return toDTO(invoice, locale, permitNumber);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getPaymentReceiptPdfFile(final long permitId,
                                                           final long invoiceId) throws IOException {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final PermitDecision permitDecision = harvestPermit.getPermitDecision();
        final Invoice invoice = invoiceRepository.getOne(invoiceId);

        permitInvoiceService.assertDecisionInvoiceForPermit(harvestPermit, invoice);

        Preconditions.checkArgument(invoice.getState() == InvoiceState.PAID);

        return PermitDecisionInvoicePdf.createReceipt(permitDecision, invoice).asResponseEntity();
    }

    @Transactional(readOnly = true)
    public List<PermitInvoiceListDTO> listByPermit(final long permitId, final InvoiceState invoiceState) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final PermitDecision permitDecision = harvestPermit.getPermitDecision();

        if (permitDecision == null) {
            return emptyList();
        }

        final String permitNumber = permitDecision.getApplication().getPermitNumber();
        final Locale locale = permitDecision.getLocale();

        return invoiceRepository.findElectronicInvoices(permitDecision, invoiceState).stream()
                .map(invoice -> toDTO(invoice, locale, permitNumber))
                .collect(Collectors.toList());
    }

    @Nonnull
    private static PermitInvoiceListDTO toDTO(final Invoice invoice, final Locale locale, final String permitNumber) {
        final PermitInvoiceListDTO dto = new PermitInvoiceListDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceType(invoice.getType());
        dto.setInvoiceDescription(String.format("%s %s",
                HarvestPermit.MOOSELIKE_PERMIT_NAME.getTranslation(locale), permitNumber));
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentDate(invoice.getPaymentDate());
        dto.setAmount(new BigDecimalMoney(invoice.getAmount()).formatPaymentAmount());

        if (invoice.getType() == InvoiceType.PERMIT_PROCESSING) {
            dto.setFrom(InvoiceContactDetailsDTO.create(invoice, invoice.getRecipientAddress()));
            dto.setTo(Locales.isSwedish(locale) ? RK_SV : RK_FI);
        }

        return dto;
    }
}
