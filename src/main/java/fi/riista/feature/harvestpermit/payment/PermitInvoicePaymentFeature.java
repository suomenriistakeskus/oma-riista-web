package fi.riista.feature.harvestpermit.payment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.PermitInvoiceService;
import fi.riista.integration.paytrail.PaytrailService;
import fi.riista.integration.paytrail.checkout.model.Customer;
import fi.riista.integration.paytrail.checkout.model.Item;
import fi.riista.integration.paytrail.checkout.model.Payment;
import fi.riista.integration.paytrail.checkout.model.PaytrailLocale;
import fi.riista.integration.paytrail.checkout.model.PaytrailPaymentInitResponse;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;
import fi.riista.integration.paytrail.util.CheckoutLogging;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

@Service
public class PermitInvoicePaymentFeature {

    private static final Logger LOG = LoggerFactory.getLogger(PermitInvoicePaymentFeature.class);

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private PermitInvoiceService permitDecisionInvoiceService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PaytrailService paytrailService;

    @Transactional(readOnly = true)
    public PaytrailPaymentInitResponse initiatePayment(final long permitId, final long invoiceId, final Locale locale) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final Invoice invoice = invoiceRepository.getOne(invoiceId);
        permitDecisionInvoiceService.assertInvoiceAttachedToPermit(harvestPermit, invoice);

        checkStartPaymentPreconditions(harvestPermit, invoice);

        LOG.info(format("Initializing payment for permitNumber=%s invoiceNumber=%s permitId=%d invoiceId=%d",
                harvestPermit.getPermitNumber(), invoice.getInvoiceNumber(), permitId, invoiceId));

        // Checkout api takes the amount in cents
        final int paytrailAmount = invoice.getAmount().multiply(BigDecimal.valueOf(100L)).intValue();

        final String invoiceTypeName = invoice.getType().getName(locale);
        final String productTitle = format("%s - %s", invoiceTypeName, harvestPermit.getPermitNumber());

        final Payment payment = new Payment();
        payment.setRedirectUrls(createCallbackUrls(harvestPermit, paytrailService::createRedirects));
        payment.setCallbackUrls(createCallbackUrls(harvestPermit, paytrailService::createCallbacks));
        paytrailService.getCallbackDelay().ifPresent(payment::setCallbackDelay);

        payment.setStamp(PaytrailOrderNumber.create(invoice, DateUtil.now()).formatAsText());
        payment.setReference(invoice.getCreditorReference().getUndelimitedValue());
        payment.setLanguage(PaytrailLocale.fromLocale(locale));  // Must be in upper case
        payment.setAmount(paytrailAmount);
        payment.setCurrency("EUR");

        // Skip customer data, supply empty email
        final Customer customer = payment.getCustomer();
        customer.setEmail("");

        final Item item = new Item();
        item.setProductCode(productTitle);
        item.setUnits(1);
        item.setUnitPrice(paytrailAmount);
        item.setVatPercentage(0);
        payment.getItems().add(item);

        return paytrailService.initiatePayment(payment, invoice.getType().getPaytrailAccount());
    }

    private <RESULT> RESULT createCallbackUrls(final HarvestPermit harvestPermit, final Function<MultiValueMap, RESULT> creatorFunction) {
        final LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("permitId", idToStringList(harvestPermit));
        return creatorFunction.apply(queryParams);
    }

    private static void checkStartPaymentPreconditions(final HarvestPermit harvestPermit, final Invoice invoice) {
        final long permitId = harvestPermit.getId();
        final long invoiceId = invoice.getId();

        if (harvestPermit.isAmendmentPermit()) {
            throw new IllegalArgumentException(format("Payment not supported for permit id=%d", permitId));
        }

        if (harvestPermit.getPermitDecision() == null) {
            throw new IllegalArgumentException(format("Decision is missing for permit id=%d", permitId));
        }

        if (harvestPermit.getPermitDecision().getApplication() == null) {
            throw new IllegalArgumentException(format("Application is missing for permit id=%d", permitId));
        }

        if (!invoice.isElectronicInvoicingEnabled()) {
            throw new IllegalArgumentException(format("Electronic invoicing not enabled for invoice id=%d", invoiceId));
        }

        if (invoice.getState() != InvoiceState.DELIVERED) {
            throw new IllegalStateException(
                    format("Invoice id=%d state is incorrect: %s", invoiceId, invoice.getState()));
        }
    }

    private static List<String> idToStringList(final HasID<Long> hasId) {
        Objects.requireNonNull(hasId);
        return Collections.singletonList(hasId.getId().toString());
    }

    private Long requireInvoice(final int invoiceNumber, final Consumer<Invoice> invoiceConsumer) {
        final Optional<Invoice> optionalInvoice = invoiceRepository.findByInvoiceNumberLocking(invoiceNumber);

        if (optionalInvoice.isPresent()) {
            invoiceConsumer.accept(optionalInvoice.get());
            return optionalInvoice.get().getId();
        } else {
            LOG.error(format("Could not find invoice by invoiceNumber=%d", invoiceNumber));
            return null;
        }
    }

    @Transactional
    public Long storePaymentSuccess(final PaytrailOrderNumber paytrailOrderNumber,
                                    final String paymentId,
                                    final String settlementReferenceNumber) {

        try {
            return requireInvoice(paytrailOrderNumber.getInvoiceNumber(), invoice -> {
                LOG.info(format("Payment successful for invoiceNumber=%s invoiceId=%d",
                        invoice.getInvoiceNumber(), invoice.getId()));

                if (invoice.getState() != InvoiceState.PAID) {
                    invoice.setPaid(DateUtil.today());
                    invoice.setPaytrailPaymentId(paymentId);
                    invoice.setPaytrailSettlementReferenceNumber(settlementReferenceNumber);

                } else {
                    LOG.warn(format("Invoice was already marked as PAID invoiceNumber=%s invoiceId=%d",
                            invoice.getInvoiceNumber(), invoice.getId()));
                }
            });
        } catch (final Exception e) {
            CheckoutLogging.logFailure(LOG, format("storePaymentSuccess failed with %s:%s", e.getClass().getName(), e.getMessage()));
        }
        return null;
    }

    @Transactional
    public void storePaymentNotify(final PaytrailOrderNumber paytrailOrderNumber,
                                   final String paymentId,
                                   final String settlementReferenceNumber) {
        try {
            requireInvoice(paytrailOrderNumber.getInvoiceNumber(), invoice -> {
                if (invoice.getState() != InvoiceState.PAID) {
                    LOG.warn(format("Invoice was not marked as PAID during notify invoiceNumber=%s invoiceId=%d",
                            invoice.getInvoiceNumber(), invoice.getId()));

                    invoice.setPaid(DateUtil.today());
                }

                if (invoice.getPaytrailPaymentId() == null) {
                    invoice.setPaytrailPaymentId(paymentId);
                }

                if (invoice.getPaytrailSettlementReferenceNumber() == null) {
                    invoice.setPaytrailSettlementReferenceNumber(settlementReferenceNumber);
                }

                // Sanity checking that settlement reference has not changed
                if (invoice.getPaytrailSettlementReferenceNumber() != null &&
                        !invoice.getPaytrailSettlementReferenceNumber().equals(settlementReferenceNumber)) {
                    LOG.warn("Settlement reference number ({}) differ from previous value ({})",
                            settlementReferenceNumber, invoice.getPaytrailSettlementReferenceNumber());
                }
            });
        } catch (final Exception e) {
            CheckoutLogging.logFailure(LOG, format("storePaymentNotify failed with %s:%s", e.getClass().getName(), e.getMessage()));
        }
    }

    @Transactional
    public void storePaymentError(final PaytrailOrderNumber paytrailOrderNumber) {
        requireInvoice(paytrailOrderNumber.getInvoiceNumber(), invoice -> {
            if (invoice.getState() == InvoiceState.DELIVERED || invoice.getState() == InvoiceState.CREATED) {
                invoice.setState(InvoiceState.UNKNOWN);

            } else {
                LOG.warn(format("Could not handle payment error for invoiceNumber=%s invoiceId=%d state=%s",
                        invoice.getInvoiceNumber(), invoice.getId(), invoice.getState()));
            }
        });
    }
}
