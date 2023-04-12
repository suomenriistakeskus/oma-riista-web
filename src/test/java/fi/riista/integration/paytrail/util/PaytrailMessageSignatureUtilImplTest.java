package fi.riista.integration.paytrail.util;

import com.google.common.collect.ImmutableMap;
import fi.riista.integration.paytrail.auth.PaytrailAuthService;
import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_SIGNATURE;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Test calculating the signature. Data taken from paytrail documentation.
 */
public class PaytrailMessageSignatureUtilImplTest {

    private PaytrailMessageSignatureUtil util;

    @Before
    public void setup() {
        final PaytrailAuthService authService = new PaytrailAuthService(new PaytrailCredentials("375917", "SAIPPUAKAUPPIAS"),
                new PaytrailCredentials("375917", "SAIPPUAKAUPPIAS"));
        util = new PaytrailMessageSignatureUtilImpl(authService);
    }

    @Test
    public void testCalculateSignature() {
        final HttpHeaders headers = initHeaders();

        final String signature = util.calculateSignature(headers, MESSAGE_BODY.getBytes(StandardCharsets.UTF_8));

        assertThat(signature, equalTo("3708f6497ae7cc55a2e6009fc90aa10c3ad0ef125260ee91b19168750f6d74f6"));
    }

    @Test
    public void testVerifyResponseSignature() {
        final HttpHeaders headers = initHeaders();
        headers.set(CHECKOUT_SIGNATURE, "3708f6497ae7cc55a2e6009fc90aa10c3ad0ef125260ee91b19168750f6d74f6");

        final byte[] bodyBytes = MESSAGE_BODY.getBytes(StandardCharsets.UTF_8);
        assertThat(util.validateResponse(headers, bodyBytes), is(true));
    }

    @Test
    public void testVerifyResponseSignature_signatureMissing() {
        final HttpHeaders headers = initHeaders();

        final byte[] bodyBytes = MESSAGE_BODY.getBytes(StandardCharsets.UTF_8);
        assertThat(util.validateResponse(headers, bodyBytes), is(false));
    }

    @Test
    public void testVerifyResponseSignature_invalidSignature() {
        final HttpHeaders headers = initHeaders();
        headers.set(CHECKOUT_SIGNATURE, "invalid");

        final byte[] bodyBytes = MESSAGE_BODY.getBytes(StandardCharsets.UTF_8);
        assertThat(util.validateResponse(headers, bodyBytes), is(false));
    }

    @Test
    public void testVerifyCallbackSignature() {
        final Map<String, String> map = initBuilder()
                .put("signature", "b2d3ecdda2c04563a4638fcade3d4e77dfdc58829b429ad2c2cb422d0fc64080")
                .build();

        assertThat(util.validateCallback(map), is(true));
    }

    @Test
    public void testVerifyCallbackSignature_signatureMissing() {
        final Map<String, String> map = initBuilder()
                .build();

        assertThat(util.validateCallback(map), is(false));
    }

    @Test
    public void testVerifyCallbackSignature_invalidSignature() {
        final Map<String, String> map = initBuilder()
                .put("signature", "invalid")
                .build();

        assertThat(util.validateCallback(map), is(false));
    }

    private static HttpHeaders initHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("checkout-account", "375917");
        headers.set("checkout-algorithm", "sha256");
        headers.set("checkout-method", "POST");
        headers.set("checkout-nonce", "564635208570151");
        headers.set("checkout-timestamp", "2018-07-06T10:01:31.904Z");
        return headers;
    }
    private static ImmutableMap.Builder<String, String> initBuilder() {
        return ImmutableMap.<String, String>builder()
                .put("checkout-account", "375917")
                .put("checkout-algorithm", "sha256")
                .put("checkout-amount", "2964")
                .put("checkout-stamp", "15336332710015")
                .put("checkout-reference", "192387192837195")
                .put("checkout-transaction-id", "4b300af6-9a22-11e8-9184-abb6de7fd2d0")
                .put("checkout-status", "ok")
                .put("checkout-provider", "nordea");
    }

    private static final String MESSAGE_BODY = "{" +
            "\"stamp\":\"unique-identifier-for-merchant\"," +
            "\"reference\":\"3759170\"," +
            "\"amount\":1525," +
            "\"currency\":\"EUR\"," +
            "\"language\":\"FI\"," +
            "\"items\":[" +
            "{" +
            "\"unitPrice\":1525," +
            "\"units\":1," +
            "\"vatPercentage\":24," +
            "\"productCode\":\"#1234\"," +
            "\"deliveryDate\":\"2018-09-01\"" +
            "}" +
            "]," +
            "\"customer\":{" +
            "\"email\":\"test.customer@example.com\"}," +
            "\"redirectUrls\":{" +
            "\"success\":\"https://ecom.example.com/cart/success\"," +
            "\"cancel\":\"https://ecom.example.com/cart/cancel\"" +
            "}" +
            "}";
}
