package fi.riista.integration.paytrail.e2;

import java.util.Objects;

public class PaytrailFormField {
    private final PaytrailFormFieldType type;
    private final Integer orderingNumber;
    private final String value;

    public PaytrailFormField(final PaytrailFormFieldType type, final String value) {
        this.type = Objects.requireNonNull(type);
        this.orderingNumber = null;
        this.value = Objects.requireNonNull(value);
    }

    public PaytrailFormField(final PaytrailFormFieldType type, final int orderingNumber, final String value) {
        this.type = Objects.requireNonNull(type);
        this.orderingNumber = orderingNumber;
        this.value = Objects.requireNonNull(value);
    }

    public String getFieldName() {
        return orderingNumber != null ? String.format("%s[%d]", type.name(), orderingNumber) : type.name();
    }

    public PaytrailFormFieldType getFieldType() {
        return type;
    }

    public String getFieldValue() {
        return value;
    }
}
