package fi.riista.integration.paytrail.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

@Component
public class PaytrailAuthService {

    @Qualifier("rk")
    @Resource
    private PaytrailCredentials rkCredentials;

    @Qualifier("mmm")
    @Resource
    private PaytrailCredentials mmmCredentials;

    public PaytrailCredentials resolveCredentials(final PaytrailAccount account) {
        switch (account) {
            case RIISTAKESKUS:
                return rkCredentials;
            case MMM:
                return mmmCredentials;
            default:
                throw new IllegalArgumentException("invalid account: " + account);
        }
    }

    public PaytrailAuthCodeVerifier createAuthCodeVerifier(final PaytrailAccount account) {
        final PaytrailCredentials credentials = resolveCredentials(account);
        return new PaytrailAuthCodeVerifier(credentials, PaytrailAuthCodeDigest.SHA256, Duration.ofHours(2));
    }

}
