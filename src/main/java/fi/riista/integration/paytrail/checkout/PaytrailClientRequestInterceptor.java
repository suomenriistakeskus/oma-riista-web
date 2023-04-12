package fi.riista.integration.paytrail.checkout;

import fi.riista.integration.paytrail.auth.InvalidSignatureException;
import fi.riista.integration.paytrail.util.PaytrailMessageSignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_RESPONSE_ID;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_SIGNATURE;

public class PaytrailClientRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(PaytrailClientRequestInterceptor.class);

    private final PaytrailMessageSignatureUtil authUtil;

    public PaytrailClientRequestInterceptor(final PaytrailMessageSignatureUtil authUtil) {
        this.authUtil = Objects.requireNonNull(authUtil);
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request,
                                        final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(CHECKOUT_SIGNATURE, authUtil.calculateSignature(request.getHeaders(), body));

        final ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode().is2xxSuccessful()) {
            return handleSuccessResponse(response);
        }

        LOG.error("Request failed {}", response.getStatusCode());
        throw new IllegalStateException("Request failed with status code " + response.getStatusCode());

    }

    private ClientHttpResponse handleSuccessResponse(final ClientHttpResponse response) throws IOException {
        // Verify response signature
        final byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
        findHeader(response, CHECKOUT_SIGNATURE)
                .map(notUsed -> authUtil.validateResponse(response.getHeaders(), responseBody))
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new InvalidSignatureException());

        findHeader(response, CHECKOUT_RESPONSE_ID).ifPresent(responseId -> {
            LOG.info("Paytrail response header found: {}={}", CHECKOUT_RESPONSE_ID, responseId);
        });

        return response;
    }

    private static Optional<String> findHeader(final ClientHttpResponse response, final String header) {
        for (final Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            if (header.equals(entry.getKey().toLowerCase())) {
                return Optional.of(String.join(", ", entry.getValue()));
            }
        }

        return Optional.empty();
    }
}

