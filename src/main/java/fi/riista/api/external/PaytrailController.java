package fi.riista.api.external;

import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.invoice.PermitInvoicePaymentFeature;
import fi.riista.integration.paytrail.callback.PaytrailCallback;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.callback.PaytrailDispatcher;
import fi.riista.integration.paytrail.event.PaytrailPaymentEventType;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RestController
@RequestMapping(PaytrailController.BASE_PATH)
public class PaytrailController {
    static final String BASE_PATH = "/api/paytrail/result/";

    public static final String SUCCESS_PATH = BASE_PATH + PaytrailPaymentEventType.SUCCESS;
    public static final String CANCEL_PATH = BASE_PATH + PaytrailPaymentEventType.CANCEL;
    public static final String NOTIFY_PATH = BASE_PATH + PaytrailPaymentEventType.NOTIFY;

    public static final String PARAMS_OUT = "ORDER_NUMBER,PAYMENT_ID,AMOUNT,CURRENCY,PAYMENT_METHOD,TIMESTAMP,STATUS,SETTLEMENT_REFERENCE_NUMBER";

    @Resource
    private PaytrailDispatcher paytrailDispatcher;

    @Resource
    private PermitInvoicePaymentFeature permitInvoicePaymentFeature;

    // Receipt always includes values PAYMENT_ID, TIMESTAMP, STATUS and RETURN_AUTHCODE.
    @GetMapping("{result}")
    public ResponseEntity<?> onResult(final @PathVariable PaytrailPaymentEventType result,
                                      final @RequestParam(value = "PAYMENT_ID") String paymentId,
                                      final @RequestParam(value = "TIMESTAMP") Long unixTimestamp,
                                      final @RequestParam(value = "STATUS") String status,
                                      final @RequestParam(value = "RETURN_AUTHCODE") String returnAuthCode,
                                      final @RequestParam(value = "ORDER_NUMBER", required = false) String orderNumber,
                                      final @RequestParam(value = "AMOUNT", required = false) String amount,
                                      final @RequestParam(value = "CURRENCY", required = false) String currency,
                                      final @RequestParam(value = "PAYMENT_METHOD", required = false) String paymentMethod,
                                      final @RequestParam(value = "SETTLEMENT_REFERENCE_NUMBER", required = false) String settlementReferenceNumber,
                                      final @RequestParam(value = "permitId", required = false) Long permitId,
                                      final HttpServletRequest httpServletRequest) {
        final PaytrailCallbackParameters paytrailParams = new PaytrailCallbackParameters(
                result, httpServletRequest.getRemoteAddr(), orderNumber, paymentId, amount, currency, paymentMethod,
                unixTimestamp, status, settlementReferenceNumber, returnAuthCode);

        return paytrailDispatcher.dispatch(paytrailParams, new PaytrailCallback() {
            @Override
            public URI onPaymentSuccess(final PaytrailOrderNumber orderNumber,
                                        final String paymentId,
                                        final String settlementReferenceNumber) {
                final Long invoiceId = permitInvoicePaymentFeature.storePaymentSuccess(orderNumber,
                        paymentId, settlementReferenceNumber);

                return permitId != null ? toReceiptView(permitId, invoiceId) : toHomePage();
            }

            @Override
            public void onPaymentNotify(final PaytrailOrderNumber orderNumber, final String paymentId,
                                        final String settlementReferenceNumber) {
                permitInvoicePaymentFeature.storePaymentNotify(orderNumber, paymentId, settlementReferenceNumber);
            }

            @Override
            public URI onPaymentCancel(final PaytrailOrderNumber orderNumber, final String paymentId) {
                return permitId != null ? toPermitDuePaymentList(permitId) : toHomePage();
            }

            @Override
            public void onPaymentAuthCodeInvalid(final PaytrailPaymentEventType type,
                                                 final PaytrailOrderNumber orderNumber) {
                if (type == PaytrailPaymentEventType.SUCCESS || type == PaytrailPaymentEventType.NOTIFY) {
                    permitInvoicePaymentFeature.storePaymentError(orderNumber);
                }
            }

            @Override
            public void onPaymentAuthCodeExpired(final PaytrailPaymentEventType type,
                                                 final PaytrailOrderNumber orderNumber) {
                if (type == PaytrailPaymentEventType.SUCCESS || type == PaytrailPaymentEventType.NOTIFY) {
                    permitInvoicePaymentFeature.storePaymentError(orderNumber);
                }
            }
        });
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
