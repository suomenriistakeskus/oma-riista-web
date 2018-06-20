package fi.riista.feature.permit.invoice;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.integration.paytrail.PaytrailService;
import fi.riista.integration.paytrail.e2.model.Payment;
import fi.riista.integration.paytrail.e2.model.Product;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;
import fi.riista.security.EntityPermission;
import fi.riista.util.BigDecimalMoney;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class PermitInvoicePaymentFeature {
    private static final Logger LOG = LoggerFactory.getLogger(PermitInvoicePaymentFeature.class);

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private PermitInvoiceService permitInvoiceService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PaytrailService paytrailService;

    @Transactional(readOnly = true)
    public Map<String, String> getPaymentForm(final long permitId,
                                              final long invoiceId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final Invoice invoice = invoiceRepository.getOne(invoiceId);
        permitInvoiceService.assertDecisionInvoiceForPermit(harvestPermit, invoice);

        checkStartPaymentPreconditions(harvestPermit, invoice);

        LOG.info(String.format("Payment form generated for permitNumber=%s invoiceNumber=%s permitId=%d invoiceId=%d",
                harvestPermit.getPermitNumber(), invoice.getInvoiceNumber(), permitId, invoiceId));

        return paytrailService.getPaymentForm(createPayment(invoice, harvestPermit));
    }

    private static void checkStartPaymentPreconditions(final HarvestPermit harvestPermit, final Invoice invoice) {
        final long permitId = harvestPermit.getId();
        final long invoiceId = invoice.getId();

        if (!harvestPermit.isMooselikePermitType()) {
            throw new IllegalArgumentException(String.format("Payment not supported for permit id=%d", permitId));
        }

        if (harvestPermit.getPermitDecision() == null) {
            throw new IllegalArgumentException(String.format("Decision is missing for permit id=%d", permitId));
        }

        if (harvestPermit.getPermitDecision().getApplication() == null) {
            throw new IllegalArgumentException(String.format("Application is missing for permit id=%d", permitId));
        }

        if (!invoice.isElectronicInvoicingEnabled()) {
            throw new IllegalArgumentException(String.format("Electronic invoicing not enabled for invoice id=%d", invoiceId));
        }

        if (invoice.getType() != InvoiceType.PERMIT_PROCESSING) {
            throw new IllegalArgumentException(String.format("Invoice id=%d type is not supported: %s",
                    invoiceId, invoice.getType()));
        }

        if (invoice.getState() != InvoiceState.DELIVERED) {
            throw new IllegalStateException(String.format("Invoice id=%d state is not incorrect: %s",
                    invoiceId, invoice.getState()));
        }
    }

    private Payment createPayment(final Invoice invoice, final HarvestPermit harvestPermit) {
        final Locale locale = harvestPermit.getPermitDecision().getLocale();
        final String paymentTitle = String.format("%s %s - %s",
                HarvestPermit.MOOSELIKE_PERMIT_NAME.getTranslation(locale),
                harvestPermit.getPermitNumber(),
                PermitDecisionInvoice.getInvoiceTypeName(locale));
        final CreditorReference creditorReference = Objects.requireNonNull(invoice.getCreditorReference());
        final String referenceNumber = creditorReference.getValue().replaceAll("[^0-9]", "");
        final BigDecimalMoney amount = new BigDecimalMoney(invoice.getAmount());

        final Payment payment = new Payment();
        payment.setOrderNumber(PaytrailOrderNumber.create(invoice, DateUtil.now()).formatAsText());
        payment.setReferenceNumber(referenceNumber);
        payment.setLocale(locale);
        payment.setVatIsIncluded(true);

        final Product product = new Product();
        payment.getProducts().add(product);
        product.setTitle(paymentTitle);
        product.setUnitPrice(amount);
        product.setVatPercent(0);

        payment.setMsgUiMerchantPanel(paymentTitle);
        payment.setSuccessUri(paytrailService.getSuccessUri(getQueryParameters(harvestPermit)));
        payment.setCancelUri(paytrailService.getCancelUri(getQueryParameters(harvestPermit)));
        payment.setNotifyUri(paytrailService.getNotifyUri());

        return payment;
    }

    private LinkedMultiValueMap<String, String> getQueryParameters(final HarvestPermit harvestPermit) {
        final LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("permitId", idToStringList(harvestPermit));
        return queryParams;
    }

    private static List<String> idToStringList(final HasID<Long> hasId) {
        Objects.requireNonNull(hasId);
        return Collections.singletonList(hasId.getId().toString());
    }

    private Long requireInvoice(final int invoiceNumber, final Consumer<Invoice> invoiceConsumer) {
        final Optional<Invoice> optionalInvoice = invoiceRepository.findByInvoiceNumber(invoiceNumber);

        if (optionalInvoice.isPresent()) {
            invoiceConsumer.accept(optionalInvoice.get());
            return optionalInvoice.get().getId();
        } else {
            LOG.error(String.format("Could not find invoice by invoiceNumber=%d", invoiceNumber));
            return null;
        }
    }

    @Transactional
    public Long storePaymentSuccess(final PaytrailOrderNumber paytrailOrderNumber, final String paymentId,
                                    final String settlementReferenceNumber) {
        return requireInvoice(paytrailOrderNumber.getInvoiceNumber(), invoice -> {
            LOG.info(String.format("Payment successful for invoiceNumber=%s invoiceId=%d",
                    invoice.getInvoiceNumber(), invoice.getId()));

            if (invoice.getState() != InvoiceState.PAID) {
                invoice.setState(InvoiceState.PAID);
                invoice.setPaymentDate(DateUtil.today());
                invoice.setPaytrailPaymentId(paymentId);
                invoice.setPaytrailSettlementReferenceNumber(settlementReferenceNumber);

            } else {
                LOG.warn(String.format("Invoice was already marked as PAID invoiceNumber=%s invoiceId=%d",
                        invoice.getInvoiceNumber(), invoice.getId()));
            }
        });
    }

    @Transactional
    public void storePaymentNotify(final PaytrailOrderNumber paytrailOrderNumber, final String paymentId,
                                   final String settlementReferenceNumber) {
        requireInvoice(paytrailOrderNumber.getInvoiceNumber(), invoice -> {
            if (invoice.getState() != InvoiceState.PAID) {
                LOG.warn(String.format("Invoice was not marked as PAID during notify invoiceNumber=%s invoiceId=%d",
                        invoice.getInvoiceNumber(), invoice.getId()));

                invoice.setState(InvoiceState.PAID);
                invoice.setPaymentDate(DateUtil.today());
            }

            if (invoice.getPaytrailPaymentId() == null) {
                invoice.setPaytrailPaymentId(paymentId);
            }

            if (invoice.getPaytrailSettlementReferenceNumber() == null) {
                invoice.setPaytrailSettlementReferenceNumber(settlementReferenceNumber);
            }
        });
    }

    @Transactional
    public void storePaymentError(final PaytrailOrderNumber paytrailOrderNumber) {
        requireInvoice(paytrailOrderNumber.getInvoiceNumber(), invoice -> {
            if (invoice.getState() == InvoiceState.DELIVERED || invoice.getState() == InvoiceState.CREATED) {
                invoice.setState(InvoiceState.UNKNOWN);

            } else {
                LOG.warn(String.format("Could not handle payment error for invoiceNumber=%s invoiceId=%d state=%s",
                        invoice.getInvoiceNumber(), invoice.getId(), invoice.getState()));
            }
        });
    }
}
