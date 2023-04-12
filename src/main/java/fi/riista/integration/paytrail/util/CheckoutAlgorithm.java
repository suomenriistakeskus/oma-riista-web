package fi.riista.integration.paytrail.util;

import java.util.Arrays;

public enum CheckoutAlgorithm {
    HMAC_SHA256("sha256"),
    HMAC_SHA512("sha512");

    private final String value;

    CheckoutAlgorithm(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CheckoutAlgorithm fromValue(final String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equals(value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
