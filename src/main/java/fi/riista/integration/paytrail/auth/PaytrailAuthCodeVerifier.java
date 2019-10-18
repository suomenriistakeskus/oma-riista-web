package fi.riista.integration.paytrail.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaytrailAuthCodeVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(PaytrailAuthCodeVerifier.class);

    private final PaytrailCredentials paytrailApiCredentials;
    private final PaytrailAuthCodeDigest paytrailAuthCodeDigest;
    private final Duration paymentTimeoutDuration;

    public PaytrailAuthCodeVerifier(final PaytrailCredentials paytrailApiCredentials,
                                    final PaytrailAuthCodeDigest paytrailAuthCodeDigest,
                                    final Duration paymentTimeoutDuration) {
        this.paytrailApiCredentials = Objects.requireNonNull(paytrailApiCredentials);
        this.paytrailAuthCodeDigest = Objects.requireNonNull(paytrailAuthCodeDigest);
        this.paymentTimeoutDuration = Objects.requireNonNull(paymentTimeoutDuration);
    }

    public void checkTimestampAge(final Long unixTimestamp) {
        final Instant now = Instant.now();
        final Instant timestamp = Instant.ofEpochSecond(unixTimestamp);
        final ZonedDateTime local = timestamp.atZone(ZoneId.of("Europe/Helsinki"));
        final Duration age = Duration.between(timestamp, now);

        if (timestamp.isBefore(now.minus(paymentTimeoutDuration))) {
            LOG.warn("Timestamp unix: {} local: {} age: {}", unixTimestamp, local, age);

            throw new PaytrailInvalidTimestampException(String.format(
                    "Timestamp %d (%s) age %s is too old", unixTimestamp, local, age));
        }
    }

    public void verifyReturnAuthCode(final String returnAuthCode,
                                     final List<String> fields) {
        final String expected = getExpectedAuthCode(fields.stream()
                .map(f -> f == null ? "" : f)
                .collect(Collectors.toList()));

        if (!expected.equalsIgnoreCase(returnAuthCode)) {
            LOG.error("Expected authCode: {} but got {}", expected, returnAuthCode);

            throw new PaytrailAuthCodeException(String.format(
                    "returnAuthCode expected: %s got: %s",
                    expected, returnAuthCode));
        }
    }

    @Nonnull
    private String getExpectedAuthCode(final List<String> fields) {
        return new PaytrailAuthCodeBuilder(paytrailApiCredentials.getMerchantSecret(), paytrailAuthCodeDigest)
                .withFields(fields)
                .getAuthCode(PaytrailAuthCodeBuilder.SecretAlignment.AFTER);
    }
}
