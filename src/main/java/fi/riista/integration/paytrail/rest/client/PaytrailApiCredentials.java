package fi.riista.integration.paytrail.rest.client;

public class PaytrailApiCredentials {
    private final String merchantId;
    private final String merchantSecret;

    public PaytrailApiCredentials(final String merchantId, final String merchantSecret) {
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
