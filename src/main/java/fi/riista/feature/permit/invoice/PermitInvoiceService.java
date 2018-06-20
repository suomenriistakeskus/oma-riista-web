package fi.riista.feature.permit.invoice;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdf;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.BigDecimalComparison;
import fi.riista.util.MediaTypeExtras;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static fi.riista.feature.permit.invoice.InvoiceStateChangeEventType.ELECTRONIC_INVOICING_DISABLED;
import static fi.riista.feature.permit.invoice.InvoiceStateChangeEventType.OVERDUE_REMINDER_CREATED;
import static java.util.Objects.requireNonNull;

@Component
public class PermitInvoiceService {

    private static final Logger LOG = LoggerFactory.getLogger(PermitInvoiceService.class);

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private InvoiceStateChangeEventRepository invoiceEventRepository;

    @Resource
    private InvoiceNumberService invoiceNumberService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

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
        final Person contactPerson = requireNonNull(decision.getContactPerson());
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        final InvoiceRecipient invoiceRecipient = InvoiceRecipient.create(decision);
        final Address invoiceAddress = addressRepository.save(new Address(contactPerson.getAddress()));
        final LocalDate invoiceDate = decision.getPublishDate().toLocalDate();
        final boolean electronicInvoicing = Boolean.FALSE.equals(application.getDeliveryByMail());

        final Invoice invoice = new Invoice(electronicInvoicing);
        invoice.setState(electronicInvoicing ? InvoiceState.DELIVERED : InvoiceState.CREATED);
        invoice.setType(InvoiceType.PERMIT_PROCESSING);
        invoice.setAmount(decision.getPaymentAmount());
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(InvoiceType.PERMIT_PROCESSING.calculateDueDate(invoiceDate));
        invoice.setInvoiceNumber(invoiceNumberService.getNextInvoiceNumber());
        invoice.setBic(PermitDecisionInvoicePdf.DECISION_BANK_ACCOUNT.getBic());
        invoice.setIban(PermitDecisionInvoicePdf.DECISION_BANK_ACCOUNT.getIban());
        invoice.setRecipientName(invoiceRecipient.getInvoiceRecipientName());
        invoice.setRecipientAddress(invoiceAddress);
        invoice.setCreditorReference(CreditorReferenceCalculator.computeReferenceForPermitDecisionProcessingInvoice(
                application.getHuntingYear(), application.getApplicationNumber()));

        // XXX For the time being, it is a bit questionable whether PDF files should be stored
        // for invoices that are going to be directly paid from within OmaRiista.
        invoice.setPdfFileMetadata(storeInvoiceFile(PermitDecisionInvoicePdf.createInvoice(decision, invoice)));

        invoiceRepository.save(invoice);

        permitDecisionInvoiceRepository.saveAndFlush(new PermitDecisionInvoice(decision, invoice));
    }

    private PersistentFileMetadata storeInvoiceFile(final PermitDecisionInvoicePdf invoicePdf) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(), invoicePdf.getData(), FileType.INVOICE_PDF,
                MediaTypeExtras.APPLICATION_PDF_VALUE, invoicePdf.getFileName());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertDecisionInvoiceForPermit(final HarvestPermit harvestPermit, final Invoice invoice) {
        if (!isInvoiceForDecision(harvestPermit.getPermitDecision(), invoice)) {
            throw new IllegalArgumentException(String.format(
                    "Invoice id=%d is not attached to permit id=%d", invoice.getId(), harvestPermit.getId()));
        }
    }

    private boolean isInvoiceForDecision(final PermitDecision decision, final Invoice invoice) {
        return permitDecisionInvoiceRepository.countByInvoiceAndDecision(decision, invoice) > 0;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getInvoicePdfFile(final Invoice invoice) throws IOException {
        return fileDownloadService.download(invoice.getPdfFileMetadata());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getReminderPdfFileAndUpdateInvoiceState(final Invoice invoice) throws IOException {
        final PermitDecision decision = permitDecisionInvoiceRepository
                .findByInvoice(invoice)
                .map(PermitDecisionInvoice::getDecision)
                .orElseThrow(() -> {
                    return new NotFoundException("Could not find decision for invoice id=" + invoice.getId());
                });

        final ResponseEntity<byte[]> response =
                PermitDecisionInvoicePdf.createReminder(decision, invoice).asResponseEntity();

        invoice.setStateReminder();
        addStateChangeEvent(invoice, OVERDUE_REMINDER_CREATED);

        return response;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void disableElectronicInvoicing(final Invoice invoice) {
        requireNonNull(invoice);
        invoice.disableElectronicInvoicing();
        addStateChangeEvent(invoice, ELECTRONIC_INVOICING_DISABLED);
    }

    private void addStateChangeEvent(final Invoice invoice, final InvoiceStateChangeEventType type) {
        invoiceEventRepository.save(new InvoiceStateChangeEvent(invoice, type));
    }
}
