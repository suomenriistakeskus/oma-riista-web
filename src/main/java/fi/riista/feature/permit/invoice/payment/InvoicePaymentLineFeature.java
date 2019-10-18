package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.search.InvoiceModeratorDTO;
import fi.riista.feature.permit.invoice.search.InvoiceModeratorDTOTransformer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.riista.feature.permit.invoice.InvoiceAuthorization.InvoicePermission.CREATE_REMOVE_PAYMENT_LINES_MANUALLY;
import static java.util.Objects.requireNonNull;

@Service
public class InvoicePaymentLineFeature {

    @Resource
    private InvoicePaymentLineRepository invoicePaymentLineRepository;

    @Resource
    private InvoicePaymentUpdaterService paymentUpdaterService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private InvoiceModeratorDTOTransformer dtoTransformer;

    @Resource
    private EnumLocaliser localiser;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional
    public void createInvoicePaymentLinesFromAccountTransfers() {
        paymentUpdaterService.createInvoicePaymentLinesFromAccountTransfers();
    }

    @Transactional
    public InvoiceModeratorDTO addInvoicePaymentLine(final long invoiceId, final AddInvoicePaymentLineDTO dto) {
        requireNonNull(dto);

        final Invoice invoice = requireEntityService.requireInvoice(invoiceId, CREATE_REMOVE_PAYMENT_LINES_MANUALLY);

        if (invoice.getType() != InvoiceType.PERMIT_HARVEST) {
            throw InvoicePaymentLineUpdateException.canAddPaymentLinesOnlyForPermitHarvestInvoices(localiser);
        }

        invoicePaymentLineRepository.save(new InvoicePaymentLine(invoice, dto.getPaymentDate(), dto.getAmount()));
        paymentUpdaterService.updateInvoicePaymentState(invoice);

        return dtoTransformer.apply(invoice);
    }

    @Transactional
    public InvoiceModeratorDTO removeInvoicePaymentLine(final long invoicePaymentLineId) {
        final InvoicePaymentLine payment = invoicePaymentLineRepository.getOne(invoicePaymentLineId);
        final Invoice invoice = payment.getInvoice();

        activeUserService.assertHasPermission(invoice, CREATE_REMOVE_PAYMENT_LINES_MANUALLY);

        if (payment.getAccountTransfer() != null) {
            throw InvoicePaymentLineUpdateException.cannotRemoveAccountTransferBasedInvoicePaymentLine(localiser);
        }

        invoicePaymentLineRepository.delete(payment);
        paymentUpdaterService.updateInvoicePaymentState(invoice);

        return dtoTransformer.apply(invoice);
    }
}
