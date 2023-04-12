package fi.riista.api.external;

import fi.riista.feature.harvestpermit.payment.PermitInvoicePaymentFeature;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.integration.paytrail.callback.PaytrailDispatcherCallback;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.callback.PaytrailDispatcher;
import fi.riista.integration.paytrail.event.PaytrailPaymentEventType;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;
import fi.riista.integration.paytrail.util.PaytrailMessageSignatureUtil;
import fi.riista.util.Collect;
import fi.riista.util.F;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_ACCOUNT;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_ALGORITHM;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_AMOUNT;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_PROVIDER;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_REFERENCE;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_SETTLEMENT_REFERENCE;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_SIGNATURE;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_STAMP;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_STATUS;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_TRANSACTION_ID;

@RestController
@RequestMapping(PaytrailController.BASE_PATH)
public class PaytrailController {
    private static final Logger LOG = LoggerFactory.getLogger(PaytrailController.class);

    static final String BASE_PATH = "/api/paytrail/result/";
    public static final String SUCCESS_PATH = BASE_PATH + PaytrailPaymentEventType.REDIRECT_SUCCESS;
    public static final String CANCEL_PATH = BASE_PATH + PaytrailPaymentEventType.REDIRECT_CANCEL;
    public static final String CALLBACK_SUCCESS_PATH = BASE_PATH + PaytrailPaymentEventType.CALLBACK_SUCCESS;
    public static final String CALLBACK_CANCEL_PATH = BASE_PATH + PaytrailPaymentEventType.CALLBACK_CANCEL;

    @Resource
    private PaytrailDispatcher paytrailDispatcher;

    @Resource
    private PermitInvoicePaymentFeature permitInvoicePaymentFeature;

    @Resource
    private PaytrailMessageSignatureUtil authUtil;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("{result}")
    public ResponseEntity<?> handleResult(
            final @PathVariable PaytrailPaymentEventType result,
            final @RequestParam(value = CHECKOUT_ACCOUNT) String account,
            final @RequestParam(value = CHECKOUT_ALGORITHM) String algorithm,
            final @RequestParam(value = CHECKOUT_STAMP) String stamp,
            final @RequestParam(value = CHECKOUT_AMOUNT) Integer amount,
            final @RequestParam(value = CHECKOUT_SETTLEMENT_REFERENCE, required = false) String settlementReference,
            final @RequestParam(value = CHECKOUT_REFERENCE) String reference,
            final @RequestParam(value = CHECKOUT_TRANSACTION_ID, required = false) String transactionId,
            final @RequestParam(value = CHECKOUT_STATUS) String status,
            final @RequestParam(value = CHECKOUT_PROVIDER, required = false) String provider,
            final @RequestParam(value = CHECKOUT_SIGNATURE, required = false) String signature,
            final @RequestParam(value = "permitId", required = false) Long permitId,
            final HttpServletRequest httpServletRequest) {

        final PaytrailOrderNumber paytrailOrderNumber = PaytrailOrderNumber.valueOf(stamp);

        final Map<String, String> parameterMap = Collections.list(httpServletRequest.getParameterNames()).stream()
                .map(name -> F.entry(name, httpServletRequest.getParameter(name)))
                .collect(Collect.entriesToMap());

        // Verify signature here since calculation needs http request parameters, handling is done at dispatcher
        final boolean validSignature = authUtil.validateCallback(parameterMap);

        final PaytrailCallbackParameters paytrailParams = new PaytrailCallbackParameters(
                result, httpServletRequest.getRemoteAddr(), stamp, transactionId,
                BigDecimal.valueOf(amount, 2).toString(), "EUR", provider,
                status, settlementReference);

        return paytrailDispatcher.dispatch(validSignature, paytrailParams, new PaytrailDispatcherCallback() {
            @Override
            public URI onRedirectSuccess(final PaytrailOrderNumber orderNumber,
                                         final String paymentId,
                                         final String settlementReferenceNumber) {
                final Long invoiceId = permitInvoicePaymentFeature.storePaymentSuccess(orderNumber,
                        paymentId, settlementReferenceNumber);

                return permitId != null && invoiceId != null ? toReceiptView(permitId, invoiceId) : toHomePage();
            }

            @Override
            public void onCallbackSuccess(final PaytrailOrderNumber orderNumber, final String paymentId,
                                          final String settlementReferenceNumber) {
                permitInvoicePaymentFeature.storePaymentNotify(paytrailOrderNumber,
                        paymentId, settlementReferenceNumber);

            }

            @Override
            public URI onRedirectCancel(final PaytrailOrderNumber orderNumber, final String paymentId) {
                return toPaymentOrHomePage(permitId);
            }

            @Override
            public URI onError(final PaytrailPaymentEventType type,
                               final PaytrailOrderNumber orderNumber) {
                permitInvoicePaymentFeature.storePaymentError(orderNumber);
                return toPaymentOrHomePage(permitId);
            }

        });
    }

    private static URI toPaymentOrHomePage(final Long permitId) {
        return permitId != null ? toPermitDuePaymentList(permitId) : toHomePage();
    }

    private static URI toReceiptView(final long permitId, final long invoiceId) {
        return PermitClientUriFactory.getRelativePermitInvoiceReceiptUri(permitId, invoiceId);
    }

    private static URI toPermitDuePaymentList(final long permitId) {
        return PermitClientUriFactory.getRelativePermitDuePaymentListUri(permitId);
    }

    private static URI toHomePage() {
        return URI.create("/");
    }
}
