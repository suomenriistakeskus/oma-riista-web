package fi.riista.integration.paytrail.auth;

public class PaytrailCredentials {
    private final String merchantId;
    private final String merchantSecret;

    public PaytrailCredentials(final String merchantId, final String merchantSecret) {
        this.merchantId = merchantId;
        this.merchantSecret = merchantSecret;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getMerchantSecret() {
        return merchantSecret;
    }
}
