package fi.riista.integration.paytrail.checkout;

import fi.riista.integration.paytrail.auth.InvalidSignatureException;
import fi.riista.integration.paytrail.util.PaytrailMessageSignatureUtil;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_SIGNATURE;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.F.mapNullable;
import static org.hamcrest.Matchers.equalTo;

public class PaytrailClientRequestInterceptorTest {

    @Test
    public void testInsertsSignature() throws IOException {
        final PaytrailClientRequestInterceptor interceptor =
                new PaytrailClientRequestInterceptor(util("54321", ""));

        final MockClientHttpRequest request = new MockClientHttpRequest();

        final byte[] bodyBytes = "body".getBytes(StandardCharsets.UTF_8);

        interceptor.intercept(request, bodyBytes, (req, body) -> {
            assertThat(req.getHeaders().getFirst(CHECKOUT_SIGNATURE), equalTo("12345"));
            final MockClientHttpResponse r = new MockClientHttpResponse(body, HttpStatus.CREATED);
            r.getHeaders().set(CHECKOUT_SIGNATURE, "54321");
            return r;
        });
    }

    @Test(expected = InvalidSignatureException.class)
    public void testFailsWithInvalidResponseSignature() throws IOException {
        final PaytrailClientRequestInterceptor interceptor =
                new PaytrailClientRequestInterceptor(util("54321", ""));

        final MockClientHttpRequest request = new MockClientHttpRequest();

        final byte[] bodyBytes = "body".getBytes(StandardCharsets.UTF_8);

        interceptor.intercept(request, bodyBytes, (req, body) -> {
            final MockClientHttpResponse r = new MockClientHttpResponse(body, HttpStatus.CREATED);
            r.getHeaders().set(CHECKOUT_SIGNATURE, "invalidSignature");
            return r;
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testFailsWithFailedResponse() throws IOException {
        final PaytrailClientRequestInterceptor interceptor =
                new PaytrailClientRequestInterceptor(util("54321", ""));

        final MockClientHttpRequest request = new MockClientHttpRequest();

        final byte[] bodyBytes = "body".getBytes(StandardCharsets.UTF_8);

        interceptor.intercept(request, bodyBytes, (req, body) ->
                new MockClientHttpResponse(body, HttpStatus.BAD_REQUEST));
    }

    private PaytrailMessageSignatureUtil util(final String expectedResponseSignature,
                                              final String expectedCallbackSignature) {
        return new DummyUtil("12345", expectedResponseSignature, expectedCallbackSignature);

    }

    private final class DummyUtil implements PaytrailMessageSignatureUtil {

        private final String signature;
        private final String expectedResponseSignature;
        private final String expectedCallbackSignature;

        public DummyUtil(final String signature, final String expectedResponseSignature, final String expectedCallbackSignature) {
            this.signature = signature;
            this.expectedResponseSignature = expectedResponseSignature;
            this.expectedCallbackSignature = expectedCallbackSignature;
        }

        @Override
        public String calculateSignature(final HttpHeaders httpHeaders, final byte[] body) {
            return signature;
        }

        @Override
        public boolean validateResponse(final HttpHeaders httpHeaders, final byte[] body) {
            final String signature = httpHeaders.getFirst(CHECKOUT_SIGNATURE);
            return mapNullable(signature, s -> s.equals(expectedResponseSignature));
        }

        @Override
        public boolean validateCallback(final Map<String, String> parameterMap) {
            final String signature = parameterMap.get(CHECKOUT_SIGNATURE);
            return mapNullable(signature, s -> s.equals(expectedCallbackSignature));
        }
    }
}
