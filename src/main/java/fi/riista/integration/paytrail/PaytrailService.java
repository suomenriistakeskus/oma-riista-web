package fi.riista.integration.paytrail;

import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.e2.model.Payment;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

public interface PaytrailService {
    Map<String, String> getPaymentForm(Payment payment);

    URI getSuccessUri(MultiValueMap<String, String> queryParameters);

    URI getCancelUri(MultiValueMap<String, String> queryParameters);

    URI getNotifyUri();

    void storePaytrailPaymentEvent(PaytrailCallbackParameters callbackParameters);
}
