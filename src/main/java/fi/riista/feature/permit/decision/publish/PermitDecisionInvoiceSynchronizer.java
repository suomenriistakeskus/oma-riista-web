package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.CreditorReferenceCalculator;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceNumberService;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoiceRepository;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdf;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.BigDecimalComparison;
import fi.riista.util.MediaTypeExtras;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Component
public class PermitDecisionInvoiceSynchronizer {
    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionInvoiceSynchronizer.class);

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private InvoiceNumberService invoiceNumberService;

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private AddressRepository addressRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void synchronizeProcessingInvoice(@Nonnull final PermitDecision decision) throws IOException {
        requireNonNull(decision);
        decision.assertStatus(PermitDecision.Status.PUBLISHED);

        final Optional<PermitDecisionInvoice> existingInvoice = permitDecisionInvoiceRepository.findByDecision(decision);

        if (existingInvoice.isPresent()) {
            updateProcessingInvoice(existingInvoice.get());

        } else if (decision.isPaymentAmountPositive()) {
            createProcessingInvoice(decision);

        } else {
            LOG.warn(String.format("Invoice generation is skipped for decisionId %d with zero payment amount", decision.getId()));
        }
    }

    private static void updateProcessingInvoice(final @Nonnull PermitDecisionInvoice permitDecisionInvoice) {
        final Invoice invoice = permitDecisionInvoice.getInvoice();
        final PermitDecision decision = permitDecisionInvoice.getDecision();
        final boolean amountMatches = BigDecimalComparison.nullsafeEq(invoice.getAmount(), decision.getPaymentAmount());

        if (amountMatches) {
            LOG.warn(String.format("Permit decision invoiceId %d already generated for decisionId %d and amount is correct %s",
                    invoice.getId(), decision.getId(), invoice.getAmount()));
            return;
        }

        LOG.warn(String.format("Permit decision invoiceId %d already generated for decisionId %d and amount should be %s but was %s",
                invoice.getId(), decision.getId(), decision.getPaymentAmount(), invoice.getAmount()));

        if (!invoice.isElectronicInvoicingEnabled()) {
            LOG.warn(String.format("Can not update invoiceId %d for decisionId %d when electronic invoicing is disabled",
                    invoice.getId(), decision.getId()));
            return;
        }

        final InvoiceState invoiceState = invoice.getState();
        final boolean invoiceInCreatedOrDeliveredState = invoiceState == InvoiceState.CREATED || invoiceState == InvoiceState.DELIVERED;

        if (!invoiceInCreatedOrDeliveredState) {
            LOG.warn(String.format("Can not update invoiceId %d in state %s decisionId: %d",
                    invoice.getId(), invoiceState, decision.getId()));
            return;
        }

        if (decision.isPaymentAmountPositive()) {
            LOG.warn(String.format("Changing invoice amount not implemented for invoiceId %d in state %s decisionId: %d",
                    invoice.getId(), invoiceState, decision.getId()));

        } else {
            // Payment amount should be zero -> make invoice VOID
            invoice.setState(InvoiceState.VOID);
        }
    }

    private void createProcessingInvoice(final @Nonnull PermitDecision decision) throws IOException {
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        final DeliveryAddress deliveryAddress = requireNonNull(decision.getDeliveryAddress());
        final Address invoiceAddress = addressRepository.save(deliveryAddress.toAddress());

        final LocalDate invoiceDate = decision.getPublishDate().toLocalDate();
        final boolean electronicInvoicing = Boolean.FALSE.equals(application.getDeliveryByMail());

        final Invoice invoice = new Invoice(InvoiceType.PERMIT_PROCESSING, electronicInvoicing);
        invoice.setState(electronicInvoicing ? InvoiceState.DELIVERED : InvoiceState.CREATED);
        invoice.setAmount(decision.getPaymentAmount());
        invoice.updateInvoiceAndDueDate(invoiceDate);
        invoice.setInvoiceNumber(invoiceNumberService.getNextInvoiceNumber());
        invoice.setIbanAndBic(FinnishBankAccount.PERMIT_DECISION_FEE_NORDEA);
        invoice.setRecipientName(deliveryAddress.getRecipient());
        invoice.setRecipientAddress(invoiceAddress);
        invoice.setCreditorReference(CreditorReferenceCalculator.computeReferenceForPermitDecisionProcessingInvoice(
                decision.getDecisionYear(), decision.getDecisionNumber()));

        invoice.setPdfFileMetadata(storeInvoiceFile(PermitDecisionInvoicePdf.createInvoice(decision, invoice)));

        invoiceRepository.save(invoice);

        permitDecisionInvoiceRepository.saveAndFlush(new PermitDecisionInvoice(decision, invoice));
    }

    private PersistentFileMetadata storeInvoiceFile(final PermitDecisionInvoicePdf invoicePdf) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(), invoicePdf.getData(), FileType.INVOICE_PDF,
                MediaTypeExtras.APPLICATION_PDF_VALUE, invoicePdf.getFileName());
    }
}
