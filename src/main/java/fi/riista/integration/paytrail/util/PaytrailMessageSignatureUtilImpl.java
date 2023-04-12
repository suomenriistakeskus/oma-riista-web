package fi.riista.integration.paytrail.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import fi.riista.integration.paytrail.auth.PaytrailAuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_ACCOUNT;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_ALGORITHM;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_PREFIX;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_SIGNATURE;

public class PaytrailMessageSignatureUtilImpl implements PaytrailMessageSignatureUtil {

    private static final byte[] EMPTY_BODY = new byte[0];

    private final PaytrailAuthService authService;

    public PaytrailMessageSignatureUtilImpl(final PaytrailAuthService authService) {
        this.authService = authService;
    }

    public String calculateSignature(final HttpHeaders httpHeaders, final byte[] body) {

        try {
            final String headers = httpHeaders.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(CHECKOUT_PREFIX))
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> mapEntry(entry))
                    .collect(Collectors.joining());

            final String merchantId = httpHeaders.getFirst(CHECKOUT_ACCOUNT);
            final CheckoutAlgorithm algorithm =
                    CheckoutAlgorithm.fromValue(httpHeaders.getFirst(CHECKOUT_ALGORITHM));
            return doCalculate(merchantId, headers, body, algorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateResponse(final HttpHeaders httpHeaders, final byte[] body) {
        final String signature = httpHeaders.getFirst(CHECKOUT_SIGNATURE);
        return StringUtils.isNotBlank(signature) && signature.equals(calculateSignature(httpHeaders, body));
    }

    public boolean validateCallback(final Map<String, String> parameterMap) {
        final String signature = parameterMap.get(CHECKOUT_SIGNATURE);

        if (StringUtils.isNotBlank(signature)) {
            final String string = parameterMap.entrySet().stream()
                    .filter(e-> e.getKey().startsWith(CHECKOUT_PREFIX))
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> formatToString(e.getKey(), e.getValue()))
                    .collect(Collectors.joining());
            final String merchantId = parameterMap.get(CHECKOUT_ACCOUNT);
            final CheckoutAlgorithm algorithm =
                    CheckoutAlgorithm.fromValue(parameterMap.get(CHECKOUT_ALGORITHM));
            return signature.equals(doCalculate(merchantId, string, EMPTY_BODY, algorithm));
        }
        return false;
    }

    private String doCalculate(final String merchantId, final String string, final byte[] body,
                               final CheckoutAlgorithm algorithm) {

        final byte[] secretBytes = authService.resolveSecret(merchantId).getBytes(StandardCharsets.UTF_8);
        final HashFunction hashFunction = algorithm == CheckoutAlgorithm.HMAC_SHA256 ?
                Hashing.hmacSha256(secretBytes) :
                Hashing.hmacSha512(secretBytes);

        return hashFunction.newHasher()
                .putString(string, StandardCharsets.UTF_8)
                .putBytes(body)
                .hash()
                .toString()
                .replace("-", "").toLowerCase();
    }

    private static <LIST extends List<String>> String mapEntry(final Map.Entry<String, LIST> entry) {
        return entry.getValue().stream()
                .filter(Objects::nonNull)
                .map(value -> formatToString(entry.getKey(), value))
                .collect(Collectors.joining());
    }

    private static String formatToString(final String key, final String value) {
        return String.format("%s:%s\n", key, value);
    }

}
