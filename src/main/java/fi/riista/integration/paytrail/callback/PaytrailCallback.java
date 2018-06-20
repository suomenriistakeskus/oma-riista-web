package fi.riista.integration.paytrail.callback;

import fi.riista.integration.paytrail.event.PaytrailPaymentEventType;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;

import java.net.URI;

public interface PaytrailCallback {

    URI onPaymentSuccess(final PaytrailOrderNumber orderNumber,
                         final String paymentId,
                         final String settlementReferenceNumber);

    void onPaymentNotify(final PaytrailOrderNumber orderNumber,
                         final String paymentId,
                         final String settlementReferenceNumber);

    URI onPaymentCancel(final PaytrailOrderNumber orderNumber, final String paymentId);

    void onPaymentAuthCodeInvalid(final PaytrailPaymentEventType type, final PaytrailOrderNumber orderNumber);

    void onPaymentAuthCodeExpired(final PaytrailPaymentEventType type, final PaytrailOrderNumber orderNumber);
}
