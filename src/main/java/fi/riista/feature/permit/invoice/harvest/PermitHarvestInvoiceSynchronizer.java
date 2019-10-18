package fi.riista.feature.permit.invoice.harvest;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceNumberService;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdaterService;
import fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdf;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.BigDecimalComparison;
import fi.riista.util.MediaTypeExtras;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.permit.invoice.CreditorReferenceCalculator.computeReferenceForPermitHarvestInvoice;
import static fi.riista.feature.permit.invoice.InvoiceState.CREATED;
import static fi.riista.feature.permit.invoice.InvoiceState.DELIVERED;
import static fi.riista.feature.permit.invoice.InvoiceState.VOID;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.NumberUtils.bigDecimalIsPositive;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Service
public class PermitHarvestInvoiceSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(PermitHarvestInvoiceSynchronizer.class);

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private PermitHarvestInvoiceRepository permitHarvestInvoiceRepository;

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private InvoiceNumberService invoiceNumberService;

    @Resource
    private InvoicePaymentUpdaterService paymentUpdaterService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void synchronizeHarvestInvoice(final HarvestPermitSpeciesAmount speciesAmount,
                                          final BigDecimal paymentAmount) throws IOException {
        requireNonNull(speciesAmount);
        requireNonNull(paymentAmount);

        final HarvestPermit permit = speciesAmount.getHarvestPermit();

        checkArgument(permit.isMooselikePermitType());

        if (permit.getPermitDecision() == null) {
            throw new IllegalArgumentException(format("Decision for permit %s is missing", permit.getPermitNumber()));
        }

        if (permit.getOriginalContactPerson().getAddress() == null) {
            throw new IllegalStateException(
                    format("Address of original contact person of permit %s is missing", permit.getPermitNumber()));
        }

        final Optional<PermitHarvestInvoice> harvestInvoiceOpt =
                permitHarvestInvoiceRepository.findBySpeciesAmount(speciesAmount);

        if (harvestInvoiceOpt.isPresent()) {
            updateInvoice(harvestInvoiceOpt.get(), paymentAmount);

        } else {
            final PermitHarvestInvoice harvestInvoice = createInvoice(speciesAmount, paymentAmount);
            paymentUpdaterService.findMatchingAccountTransfersAndCreateInvoicePaymentLines(harvestInvoice.getInvoice());
        }
    }

    private PermitHarvestInvoice createInvoice(final HarvestPermitSpeciesAmount speciesAmount,
                                               final BigDecimal paymentAmount) throws IOException {

        final int huntingYear = speciesAmount.resolveHuntingYear();
        final GameSpecies species = speciesAmount.getGameSpecies();
        final HarvestPermit permit = speciesAmount.getHarvestPermit();

        final Integer applicationNumber = permit.getPermitDecision().getDecisionNumber();
        final DeliveryAddress deliveryAddress = requireNonNull(permit.getPermitDecision().getDeliveryAddress());
        final CreditorReference creditorReference =
                computeReferenceForPermitHarvestInvoice(huntingYear, applicationNumber, species.getOfficialCode());

        final Person contactPerson = permit.getOriginalContactPerson();

        final Invoice invoice = new Invoice(InvoiceType.PERMIT_HARVEST, true);
        invoice.setState(bigDecimalIsPositive(paymentAmount) ? DELIVERED : VOID);
        invoice.setInvoiceNumber(invoiceNumberService.getNextInvoiceNumber());
        invoice.updateInvoiceAndDueDate(today());
        invoice.setAmount(paymentAmount);
        invoice.setIbanAndBic(FinnishBankAccount.MOOSELIKE_HARVEST_FEE_OP_POHJOLA);
        invoice.setCreditorReference(creditorReference);
        invoice.setRecipientName(deliveryAddress.getRecipient());
        invoice.setRecipientAddress(addressRepository.save(deliveryAddress.toAddress()));
        invoice.setPdfFileMetadata(storeInvoicePdf(speciesAmount, invoice));

        invoiceRepository.save(invoice);

        return permitHarvestInvoiceRepository.save(new PermitHarvestInvoice(invoice, speciesAmount));
    }

    private void updateInvoice(final PermitHarvestInvoice harvestInvoice,
                               final BigDecimal paymentAmount) throws IOException {

        final HarvestPermitSpeciesAmount speciesAmount = harvestInvoice.getSpeciesAmount();
        final Invoice invoice = harvestInvoice.getInvoice();
        final boolean amountMatches = BigDecimalComparison.nullsafeEq(invoice.getAmount(), paymentAmount);

        if (amountMatches) {
            LOG.warn("Invoice (id={}) already generated for speciesAmountId={} with matching amount {}",
                    invoice.getId(), speciesAmount.getId(), invoice.getAmount());

        } else {
            LOG.warn("Invoice (id={}) already generated for speciesAmountId={} and amount should be {} but was {}",
                    invoice.getId(), speciesAmount.getId(), paymentAmount, invoice.getAmount());
        }

        // Payment details/terms (invoice date, due date, amount, PDF file) are updateable as long
        // as invoice is still not transitioned to PAID/REMINDER state and electronic invoicing is
        // not disabled.
        if (hasValidInvoicingMethodForUpdate(harvestInvoice) && hasValidInvoiceStateForUpdate(harvestInvoice)) {

            // Payment amount is zero? -> make invoice VOID
            invoice.setState(bigDecimalIsPositive(paymentAmount) ? DELIVERED : VOID);

            Optional.ofNullable(invoice.getCorrectedAmount()).ifPresent(correctedAmount -> {
                LOG.warn("Corrected amount for invoice (id={}) was unexpectedly not null: {}. Setting it to null.",
                        invoice.getId(), correctedAmount);
                invoice.setCorrectedAmount(null);
            });

            if (!amountMatches) {
                // Update invoice payment terms
                invoice.updateInvoiceAndDueDate(today());
                invoice.setAmount(paymentAmount);

                // Update invoice PDF
                final UUID toBeDeleteFileUuid = invoice.getPdfFileMetadata().getId();
                invoice.setPdfFileMetadata(storeInvoicePdf(speciesAmount, invoice));
                invoiceRepository.flush();

                fileStorageService.remove(toBeDeleteFileUuid);
            }
        } else {
            // When invoice has transitioned to PAID state (by being paid via Paytrail or when
            // account statement confirmation of a payment is received) original invoice payment
            // details are not changed. Instead, corrected amount will be updated. This can
            // happen e.g. when permit holder (1) pays harvest invoice, (2) cancels end of
            // moose hunting and (3) performs it again.

            invoice.setCorrectedAmount(amountMatches ? null : paymentAmount);
        }
    }

    private PersistentFileMetadata storeInvoicePdf(final HarvestPermitSpeciesAmount speciesAmount,
                                                   final Invoice invoice) throws IOException {

        final PermitHarvestInvoicePdf pdf = PermitHarvestInvoicePdf.createInvoice(speciesAmount, invoice);

        return fileStorageService.storeFile(UUID.randomUUID(), pdf.getData(), FileType.INVOICE_PDF,
                MediaTypeExtras.APPLICATION_PDF_VALUE, pdf.getFileName());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void cancelInvoice(final HarvestPermitSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount);

        final HarvestPermit permit = speciesAmount.getHarvestPermit();

        requireNonNull(permit.getPermitDecision());
        checkArgument(permit.isMooselikePermitType());
        checkArgument(speciesAmount.getGameSpecies().isMooseOrDeerRequiringPermitForHunting());

        final Optional<PermitHarvestInvoice> existingInvoice =
                permitHarvestInvoiceRepository.findBySpeciesAmount(speciesAmount);

        if (existingInvoice.isPresent()) {
            final PermitHarvestInvoice harvestInvoice = existingInvoice.get();

            if (hasValidInvoicingMethodForUpdate(harvestInvoice) && hasValidInvoiceStateForUpdate(harvestInvoice)) {
                harvestInvoice.getInvoice().setState(VOID);
            }
        } else {
            LOG.warn("No invoice to be cancelled exists for speciesAmountId: {}", speciesAmount.getId());
        }
    }

    private static boolean hasValidInvoiceStateForUpdate(final PermitHarvestInvoice harvestInvoice) {
        final Invoice invoice = harvestInvoice.getInvoice();
        final InvoiceState invoiceState = invoice.getState();
        final boolean validState = invoiceState == CREATED || invoiceState == DELIVERED || invoiceState == VOID;

        if (!validState) {
            LOG.warn("Cannot update payment state and terms for invoice (id={}, speciesAmountId={}) in state {}",
                    invoice.getId(), harvestInvoice.getSpeciesAmount().getId(), invoiceState);
        }

        return validState;
    }

    private static boolean hasValidInvoicingMethodForUpdate(final PermitHarvestInvoice harvestInvoice) {
        final Invoice invoice = harvestInvoice.getInvoice();

        if (!invoice.isElectronicInvoicingEnabled()) {
            LOG.warn("Cannot update payment state and terms for invoice (id={}, speciesAmountId={}) when electronic invoicing is disabled",
                    invoice.getId(), harvestInvoice.getSpeciesAmount().getId());
            return false;
        }

        return true;
    }
}
