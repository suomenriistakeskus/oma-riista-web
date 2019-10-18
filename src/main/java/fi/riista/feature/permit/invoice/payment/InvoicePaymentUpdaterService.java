package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferRepository;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static fi.riista.util.Collect.mappingTo;
import static fi.riista.util.NumberUtils.sum;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;

@Component
public class InvoicePaymentUpdaterService {

    private static final Logger LOG = LoggerFactory.getLogger(InvoicePaymentUpdaterService.class);

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private InvoicePaymentLineRepository paymentLineRepository;

    @Resource
    private AccountTransferRepository transferRepository;

    @Resource
    private EnumLocaliser localiser;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void findMatchingAccountTransfersAndCreateInvoicePaymentLines(final Invoice invoice) {
        final List<AccountTransfer> matchingAccountTransfers =
                transferRepository.findAccountTransfersNotAssociatedWithInvoice(invoice.getCreditorReference());

        if (matchingAccountTransfers.isEmpty()) {
            LOG.info("No matching account transfers found for invoiceId={}", invoice.getId());

        } else {
            addInvoicePaymentLines(invoice, matchingAccountTransfers);
            updateInvoicePaymentState(invoice);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void createInvoicePaymentLinesFromAccountTransfers() {
        final List<AccountTransfer> transfers = transferRepository.findAccountTransfersNotAssociatedWithInvoice();

        if (transfers.isEmpty()) {
            LOG.info("Did not find account transfers to be associated with invoices.");

        } else {
            final Map<CreditorReference, List<AccountTransfer>> transfersByReference =
                    transfers.stream().collect(groupingBy(AccountTransfer::getCreditorReference));

            final List<Invoice> invoices = invoiceRepository.findByCreditorReferences(transfersByReference.keySet());

            if (invoices.isEmpty()) {
                LOG.info("Did not find matching invoices for currently unassociated account transfers.");
                
            } else {
                LOG.info("Associating {} account transfers with {} invoices.", transfers.size(), invoices.size());

                // Create InvoicePaymentLines for yet unassociated AccountTransfers.
                invoices.stream()
                        .collect(mappingTo(invoice -> transfersByReference.get(invoice.getCreditorReference())))
                        .forEach(this::addInvoicePaymentLines);

                paymentLineRepository
                        .findAndGroupByInvoices(invoices)
                        .forEach(this::updateInvoicePaymentStateFromPaymentLines);

                LOG.info("Updated received money sum for {} invoices.", invoices.size());
            }
        }
    }

    private void addInvoicePaymentLines(final Invoice invoice, final List<AccountTransfer> transfers) {
        transfers.forEach(transfer -> paymentLineRepository.save(new InvoicePaymentLine(invoice, transfer)));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateInvoicePaymentState(final Invoice invoice) {
        final List<InvoicePaymentLine> paymentLines = paymentLineRepository.findByInvoice(invoice);

        if (paymentLines.isEmpty()) {
            invoice.setReceivedAmount(null);

            if (!invoice.isPaytrailPaymentInitiated()) {
                // Reset invoice state if last moderator-inserted payment line is removed and
                // if payment is not paid via Paytrail.

                invoice.setState(InvoiceState.DELIVERED);
                invoice.setPaymentDate(null);
            }
        } else {
            updateInvoicePaymentStateFromPaymentLines(invoice, paymentLines);
        }
    }

    private void updateInvoicePaymentStateFromPaymentLines(final Invoice invoice,
                                                           final List<InvoicePaymentLine> paymentLines) {

        checkState(paymentLines.size() > 0, "paymentLines must be non-empty");

        final BigDecimal totalPaymentAmount = sum(paymentLines, InvoicePaymentLine::getAmount);

        if (totalPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw InvoicePaymentLineUpdateException.sumMustNotBeNegative(localiser);
        }

        invoice.setReceivedAmount(totalPaymentAmount);

        // Get paymentDate from the latest InvoicePaymentLine.
        final LocalDate latestDateFromPaymentLines = paymentLines.stream()
                .map(InvoicePaymentLine::getPaymentDate)
                .max(naturalOrder())
                .orElseThrow(() -> new IllegalStateException(
                        "Resolving paymentDate from InvoicePaymentLines failed for invoiceId=" + invoice.getId()));

        final InvoiceState state = invoice.getState();

        if (state == InvoiceState.CREATED || state == InvoiceState.DELIVERED || state == InvoiceState.VOID) {
            invoice.setPaid(latestDateFromPaymentLines);

        } else if (paymentLines.size() > 1
                || invoice.getPaymentDate() != null && latestDateFromPaymentLines.isBefore(invoice.getPaymentDate())) {

            // The conditions above ascertain that payment date determined by the moment of
            // committing Paytrail transaction is not overridden by transaction date of account
            // transfer if there is no more than one present. On the other hand, payment date must
            // be updated if the most recent one of multiple moderator-inserted payment lines
            // is removed.

            invoice.setPaymentDate(latestDateFromPaymentLines);
        }
    }
}
