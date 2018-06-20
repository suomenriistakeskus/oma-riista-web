package fi.riista.integration.paytrail.rest;

import fi.riista.integration.paytrail.rest.client.PaytrailRestTemplate;
import fi.riista.integration.paytrail.rest.model.CreatePaymentRequest;
import fi.riista.integration.paytrail.rest.model.CreatePaymentResponse;

import java.net.URI;

public class PaytrailRestAdapter {
    public static final URI REST_PAYMENT_URI = URI.create("https://payment.paytrail.com/api-payment/create");

    private PaytrailRestTemplate paytrailRestTemplate;

    public PaytrailRestAdapter(final PaytrailRestTemplate paytrailRestTemplate) {
        this.paytrailRestTemplate = paytrailRestTemplate;
    }

    public CreatePaymentResponse createPayment(final CreatePaymentRequest request) {
        return paytrailRestTemplate.postForObject(REST_PAYMENT_URI, request, CreatePaymentResponse.class);
    }
}
