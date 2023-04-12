package fi.riista.integration.paytrail.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.stream.Stream;

public class PaytrailAuthService {

    private final PaytrailCredentials rkCredentials;

    private final PaytrailCredentials mmmCredentials;

    public PaytrailAuthService(final PaytrailCredentials rkCredentials, final PaytrailCredentials mmmCredentials) {
        this.rkCredentials = rkCredentials;
        this.mmmCredentials = mmmCredentials;
    }

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

    public String resolveSecret(final String merchantId) {
        return Stream.of(rkCredentials, mmmCredentials)
                .filter(creds -> creds.getMerchantId().equals(merchantId))
                .map(PaytrailCredentials::getMerchantSecret)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown merchant id " + merchantId));
    }

}
