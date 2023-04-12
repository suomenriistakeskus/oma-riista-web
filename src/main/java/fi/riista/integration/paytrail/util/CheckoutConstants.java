package fi.riista.integration.paytrail.util;

import java.util.Optional;

public class CheckoutConstants {

    public static final String PAYTRAIL_SERVICE_URI = "https://services.paytrail.com/payments";
    public static final String CHECKOUT_PREFIX = "checkout-";
    public static final String CHECKOUT_ACCOUNT = "checkout-account";
    public static final String CHECKOUT_ALGORITHM = "checkout-algorithm";
    public static final String CHECKOUT_METHOD = "checkout-method";
    public static final String CHECKOUT_NONCE = "checkout-nonce";
    public static final String CHECKOUT_TIMESTAMP = "checkout-timestamp";
    public static final String CHECKOUT_STAMP = "checkout-stamp";
    public static final String CHECKOUT_AMOUNT = "checkout-amount";
    public static final String CHECKOUT_SETTLEMENT_REFERENCE = "checkout-settlement-reference";
    public static final String CHECKOUT_REFERENCE = "checkout-reference";
    public static final String CHECKOUT_TRANSACTION_ID = "checkout-transaction-id";
    public static final String CHECKOUT_STATUS = "checkout-status";
    public static final String CHECKOUT_PROVIDER = "checkout-provider";
    public static final String CHECKOUT_SIGNATURE = "signature";
    public static final String CHECKOUT_RESPONSE_ID = "request-id";

    public static final String CHECKOUT_PAYMENT_STATUS_OK = "ok";
    public static final Optional<Integer> CHECKOUT_CALLBACK_DELAY_SECONDS = Optional.of(15);

    private CheckoutConstants() {
        throw new UnsupportedOperationException();
    }
}
