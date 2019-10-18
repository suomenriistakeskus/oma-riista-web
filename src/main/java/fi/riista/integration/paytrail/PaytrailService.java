package fi.riista.integration.paytrail;

import fi.riista.integration.paytrail.auth.PaytrailAccount;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.e2.model.CallbackUrlSet;
import fi.riista.integration.paytrail.e2.model.Payment;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface PaytrailService {
    Map<String, String> getPaymentForm(Payment payment, PaytrailAccount account);

    CallbackUrlSet createCallbacks(MultiValueMap<String, String> queryParameters);

    void storePaytrailPaymentEvent(PaytrailCallbackParameters callbackParameters);
}
