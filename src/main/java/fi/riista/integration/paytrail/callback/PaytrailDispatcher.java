package fi.riista.integration.paytrail.callback;

import fi.riista.integration.paytrail.PaytrailService;
import fi.riista.integration.paytrail.auth.PaytrailAuthCodeException;
import fi.riista.integration.paytrail.auth.PaytrailAuthCodeVerifier;
import fi.riista.integration.paytrail.auth.PaytrailInvalidTimestampException;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;

@Component
public class PaytrailDispatcher {

    @Resource
    private PaytrailAuthCodeVerifier authCodeVerifier;

    @Resource
    private PaytrailService paytrailService;

    public ResponseEntity<?> dispatch(final PaytrailCallbackParameters params, final PaytrailCallback callback) {
        paytrailService.storePaytrailPaymentEvent(params);

        final PaytrailOrderNumber paytrailOrderNumber = PaytrailOrderNumber.valueOf(params.getOrderNumber());

        try {
            authCodeVerifier.checkTimestampAge(params.getUnixTimestamp());
            authCodeVerifier.verifyReturnAuthCode(params.getReturnAuthCode(),
                    params.getFieldsForReturnAuthCodeValidation());

            switch (params.getType()) {
                case SUCCESS:
                    return redirect(callback.onPaymentSuccess(
                            paytrailOrderNumber,
                            params.getPaymentId(),
                            params.getSettlementReferenceNumber()));

                case CANCEL:
                    return redirect(callback.onPaymentCancel(
                            paytrailOrderNumber,
                            params.getPaymentId()));

                case NOTIFY:
                    callback.onPaymentNotify(
                            paytrailOrderNumber,
                            params.getPaymentId(),
                            params.getSettlementReferenceNumber());

                default:
                    return ResponseEntity.ok("ok");
            }

        } catch (PaytrailAuthCodeException e) {
            callback.onPaymentAuthCodeInvalid(params.getType(), paytrailOrderNumber);
            return ResponseEntity.ok("auth-code-invalid");

        } catch (PaytrailInvalidTimestampException e) {
            callback.onPaymentAuthCodeExpired(params.getType(), paytrailOrderNumber);
            return ResponseEntity.ok("auth-code-expired");
        }
    }

    private static ResponseEntity<?> redirect(URI to) {
        return ResponseEntity.status(HttpStatus.FOUND).location(to).build();
    }
}
