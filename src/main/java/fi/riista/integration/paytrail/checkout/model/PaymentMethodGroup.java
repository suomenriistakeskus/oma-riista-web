package fi.riista.integration.paytrail.checkout.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentMethodGroup {

    MOBILE("mobile"),
    BANK("bank"),
    CREDIT_CARD("creditcard"),
    CREDIT("credit");

    private final String value;

    PaymentMethodGroup(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @JsonCreator
    public static PaymentMethodGroup fromValue(String v) {
        for (PaymentMethodGroup c : PaymentMethodGroup.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
