package fi.riista.integration.fivaldi;

public enum FivaldiPaymentMethod {

    PAYTRAIL(FivaldiConstants.FIVALDI_CUSTOMER_ID_PAYTRAIL),
    PAPER(FivaldiConstants.FIVALDI_CUSTOMER_ID_PAPER);

    final String customerId;

    FivaldiPaymentMethod(final String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }
}
