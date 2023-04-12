package fi.riista.integration.paytrail;

import fi.riista.integration.paytrail.auth.PaytrailAccount;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.checkout.model.CallbackUrl;
import fi.riista.integration.paytrail.checkout.model.Payment;
import fi.riista.integration.paytrail.checkout.model.PaytrailPaymentInitResponse;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

public interface PaytrailService {

    PaytrailPaymentInitResponse initiatePayment(Payment payment, PaytrailAccount account);

    CallbackUrl createCallbacks(MultiValueMap<String, String> queryParameters);
    CallbackUrl createRedirects(MultiValueMap<String, String> queryParameters);

    void storePaytrailPaymentEvent(final PaytrailCallbackParameters paytrailParams);

    Optional<Integer> getCallbackDelay();
}
