package fi.riista.integration.paytrail.callback;

import fi.riista.integration.paytrail.order.PaytrailOrderNumber;
import fi.riista.integration.paytrail.PaytrailService;
import fi.riista.integration.paytrail.util.CheckoutConstants;
import fi.riista.integration.paytrail.util.CheckoutLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;

@Component
public class PaytrailDispatcher {

    private static Logger LOG = LoggerFactory.getLogger(PaytrailDispatcher.class);
    @Resource
    private PaytrailService paytrailService;

    public ResponseEntity<?> dispatch(final boolean validSignature,
                                      final PaytrailCallbackParameters params,
                                      final PaytrailDispatcherCallback callback) {
        if (validSignature) {
            paytrailService.storePaytrailPaymentEvent(params);

            final PaytrailOrderNumber paytrailOrderNumber = PaytrailOrderNumber.valueOf(params.getOrderNumber());

            switch (params.getType()) {
                case REDIRECT_SUCCESS:
                    if (CheckoutConstants.CHECKOUT_PAYMENT_STATUS_OK.equals(params.getStatus())) {
                        return redirect(callback.onRedirectSuccess(
                                paytrailOrderNumber,
                                params.getPaymentId(),
                                params.getSettlementReferenceNumber()));
                    } else {
                        CheckoutLogging.logFailure(LOG, "REDIRECT_SUCCESS received with status " + params.getStatus());
                        return redirect(callback.onError(params.getType(), paytrailOrderNumber));
                    }
                case REDIRECT_CANCEL:
                    return redirect(callback.onRedirectCancel(
                            paytrailOrderNumber,
                            params.getPaymentId()));
                case CALLBACK_SUCCESS: {
                    if (CheckoutConstants.CHECKOUT_PAYMENT_STATUS_OK.equals(params.getStatus())) {
                        callback.onCallbackSuccess(paytrailOrderNumber,
                                params.getPaymentId(),
                                params.getSettlementReferenceNumber());
                    } else {
                        LOG.info("Callback success with status {} for orderNumber {}",
                                params.getStatus(), params.getOrderNumber());
                        return ResponseEntity
                                .badRequest()
                                .body("Success callback called with status " + params.getStatus());
                    }
                    return ResponseEntity.ok("ok");
                }
                default:
                    return ResponseEntity.ok("ok");
            }

        } else {
            CheckoutLogging.logFailure(LOG, "Invalid signature while handling " + params.getType());
            return ResponseEntity.badRequest().body("Invalid checksum");
        }
    }

    private static ResponseEntity<?> redirect(URI to) {
        return ResponseEntity.status(HttpStatus.FOUND).location(to).build();
    }
}
