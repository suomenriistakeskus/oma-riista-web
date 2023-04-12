package fi.riista.integration.paytrail.callback;

import fi.riista.integration.paytrail.event.PaytrailPaymentEventType;
import fi.riista.integration.paytrail.order.PaytrailOrderNumber;

import java.net.URI;

public interface PaytrailDispatcherCallback {

    // Dispatcher calls this when client redirected to success URL
    URI onRedirectSuccess(final PaytrailOrderNumber orderNumber,
                          final String paymentId,
                          final String settlementReferenceNumber);

    // Dispatcher calls this on Paytrail success callback
    void onCallbackSuccess(final PaytrailOrderNumber orderNumber,
                           final String paymentId,
                           final String settlementReferenceNumber);

    // Dispatcher calls this when client redirected to cancel URL
    URI onRedirectCancel(final PaytrailOrderNumber orderNumber, final String paymentId);

    // Dispatcher calls this when some assertion is not satified during client redirect handling
    URI onError(final PaytrailPaymentEventType type, final PaytrailOrderNumber orderNumber);

}
