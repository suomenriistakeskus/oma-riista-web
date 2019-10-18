package fi.riista.feature.harvestpermit.payment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceContactDetailsDTO;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.PermitInvoiceService;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoice;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceRepository;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdf;
import fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdf;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import fi.riista.util.RiistakeskusConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class PermitInvoiceListFeature {

    private static final InvoiceContactDetailsDTO RK_FI = InvoiceContactDetailsDTO.create(
            RiistakeskusConstants.NAME.getFinnish(),
            RiistakeskusConstants.STREET_ADDRESS.getFinnish(),
            RiistakeskusConstants.POST_OFFICE.getFinnish(),
            RiistakeskusConstants.PHONE_NUMBER);

    private static final InvoiceContactDetailsDTO RK_SV = InvoiceContactDetailsDTO.create(
            RiistakeskusConstants.NAME.getSwedish(),
            RiistakeskusConstants.STREET_ADDRESS.getSwedish(),
            RiistakeskusConstants.POST_OFFICE.getSwedish(),
            RiistakeskusConstants.PHONE_NUMBER);

    private static final InvoiceContactDetailsDTO MMM = InvoiceContactDetailsDTO.create(
            "Maa- ja metsätalousministeriö",
            "",
            "",
            "");

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitInvoiceService permitInvoiceService;

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private PermitHarvestInvoiceRepository permitHarvestInvoiceRepository;

    @Transactional(readOnly = true)
    public PermitInvoiceListDTO getPermitInvoice(final long permitId, final long invoiceId, final Locale locale) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final Invoice invoice = invoiceRepository.getOne(invoiceId);

        permitInvoiceService.assertInvoiceAttachedToPermit(harvestPermit, invoice);

        return toDTO(invoice, harvestPermit, locale);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getPaymentReceiptPdfFile(final long permitId,
                                                           final long invoiceId) throws IOException {

        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final Invoice invoice = invoiceRepository.getOne(invoiceId);

        permitInvoiceService.assertInvoiceAttachedToPermit(permit, invoice);

        checkArgument(invoice.isReceiptAvailable());

        switch (invoice.getType()) {
            case PERMIT_PROCESSING:
                return PermitDecisionInvoicePdf.createReceipt(permit.getPermitDecision(), invoice).asResponseEntity();

            case PERMIT_HARVEST:
                final HarvestPermitSpeciesAmount speciesAmount = permitHarvestInvoiceRepository
                        .findByInvoice(invoice)
                        .map(PermitHarvestInvoice::getSpeciesAmount)
                        .orElseThrow(IllegalArgumentException::new);
                return PermitHarvestInvoicePdf.createReceipt(speciesAmount, invoice).asResponseEntity();

            default:
                throw new IllegalArgumentException("Invoice type not supported: " + invoice.getType());
        }
    }

    @Transactional(readOnly = true)
    public List<PermitInvoiceListDTO> listDueByPermit(final long permitId, final Locale locale) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        return invoiceRepository
                .findElectronicInvoices(permit, InvoiceState.DELIVERED)
                .stream()
                .map(invoice -> toDTO(invoice, permit, locale))
                .sorted(comparing(PermitInvoiceListDTO::getDueDate)
                        .thenComparing(PermitInvoiceListDTO::getInvoiceDate)
                        .thenComparing(PermitInvoiceListDTO::getId))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PermitInvoiceListDTO> listPaidByPermit(final long permitId, final Locale locale) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        final List<Invoice> processingInvoices = invoiceRepository
                .findElectronicInvoices(permit, EnumSet.of(InvoiceType.PERMIT_PROCESSING), InvoiceState.PAID);
        final List<Invoice> harvestInvoices =
                invoiceRepository.findHarvestInvoicesHavingInitiatedOrConfirmedPayments(permit);

        return Stream
                .concat(processingInvoices.stream(), harvestInvoices.stream())
                .map(invoice -> toDTO(invoice, permit, locale))
                .sorted(comparing(PermitInvoiceListDTO::getPaymentDate)
                        .thenComparing(PermitInvoiceListDTO::getDueDate)
                        .thenComparing(PermitInvoiceListDTO::getInvoiceDate)
                        .thenComparing(PermitInvoiceListDTO::getId)
                        .reversed())
                .collect(toList());
    }

    @Nonnull
    private static PermitInvoiceListDTO toDTO(final Invoice invoice, final HarvestPermit permit, final Locale locale) {
        final PermitInvoiceListDTO dto = new PermitInvoiceListDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceType(invoice.getType());
        dto.setInvoiceDescription(String.format("%s %s", permit.getPermitType(), permit.getPermitNumber()));
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentDate(invoice.getPaymentDate());

        if (invoice.getCorrectedAmount() != null) {
            dto.setAmount(invoice.getCorrectedAmount());
            dto.setCorrected(true);
        } else {
            dto.setAmount(invoice.getAmount());
            dto.setCorrected(false);
        }

        dto.setPaidAmount(invoice.getReceiptAmount());
        dto.setFrom(InvoiceContactDetailsDTO.create(invoice, invoice.getRecipientAddress()));

        switch (invoice.getType()) {
            case PERMIT_PROCESSING:
                dto.setTo(Locales.isSwedish(locale) ? RK_SV : RK_FI);
                break;
            case PERMIT_HARVEST:
                dto.setTo(MMM);
                break;
        }

        return dto;
    }
}
