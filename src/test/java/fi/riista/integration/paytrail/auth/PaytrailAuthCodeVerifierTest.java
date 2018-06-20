package fi.riista.integration.paytrail.auth;

import fi.riista.integration.paytrail.rest.client.PaytrailApiCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class PaytrailAuthCodeVerifierTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Nonnull
    private PaytrailAuthCodeVerifier createTestVerifier() {
        final PaytrailApiCredentials credentials = new PaytrailApiCredentials(
                "13466", "6pKF4jkv97zmqBJ3ZL8gUw5DfT2NMQ");
        return new PaytrailAuthCodeVerifier(credentials, PaytrailAuthCodeDigest.SHA256, Duration.ofMinutes(1));
    }

    @Test
    public void testAuthCodeFromDocumentation() {
        final List<String> fields = Arrays.asList(
                "ORDER-12345",
                "123456789012",
                "200.00",
                "1491896573",
                "PAID"
        );

        createTestVerifier().verifyReturnAuthCode("86CC6A9B9433D3AC1D8D1B8D21ED87DA3ABE2E980D3F826D1901FEF0925F5D03", fields);
    }

    @Test
    public void testAuthCodeSuccess() {
        final List<String> fields = Arrays.asList(
                "546015a3-ba53-41db-bbd5-2fdbeca32f33",
                "103673337803",
                "90.00",
                "EUR",
                "1",
                "1525286841",
                "PAID"
        );

        createTestVerifier().verifyReturnAuthCode("C89C341457F7E8CBF2E21E868595F929A0FC9EB37C9695C78A061612542EAB28", fields);
    }

    @Test
    public void testAuthCodeFailure() {
        final List<String> fields = Arrays.asList(
                "546015a3-ba53-41db-bbd5-2fdbeca32f33",
                "103673337803",
                "90.00",
                "EUR",
                "2",
                "1525286841",
                "PAID"
        );

        thrown.expect(PaytrailAuthCodeException.class);
        thrown.expectMessage("returnAuthCode expected: DB570AF82558BA516E3D677BE78866B2B02C8D0E3FED22AD535B13DBF2C99DBF got: C89C341457F7E8CBF2E21E868595F929A0FC9EB37C9695C78A061612542EAB28");

        createTestVerifier().verifyReturnAuthCode("C89C341457F7E8CBF2E21E868595F929A0FC9EB37C9695C78A061612542EAB28", fields);
    }

    @Test
    public void testTimestampRecent() {
        createTestVerifier().checkTimestampAge(System.currentTimeMillis());
    }

    @Test
    public void testTimestampTooOld() {
        thrown.expect(PaytrailInvalidTimestampException.class);

        createTestVerifier().checkTimestampAge(1525286841L);
    }
}
