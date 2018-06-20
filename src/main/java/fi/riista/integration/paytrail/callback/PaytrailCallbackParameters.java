package fi.riista.integration.paytrail.callback;

import fi.riista.integration.paytrail.event.PaytrailPaymentEventType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PaytrailCallbackParameters {
    private final PaytrailPaymentEventType type;
    private final String remoteAddress;
    private final String orderNumber;
    private final String paymentId;
    private final String amount;
    private final String currency;
    private final String paymentMethod;
    private final Long unixTimestamp;
    private final String status;
    private final String settlementReferenceNumber;
    private final String returnAuthCode;

    public PaytrailCallbackParameters(final PaytrailPaymentEventType type,
                                      final String remoteAddress,
                                      final String orderNumber,
                                      final String paymentId,
                                      final String amount,
                                      final String currency,
                                      final String paymentMethod,
                                      final Long unixTimestamp,
                                      final String status,
                                      final String settlementReferenceNumber,
                                      final String returnAuthCode) {
        this.type = Objects.requireNonNull(type);
        this.remoteAddress = remoteAddress;
        this.orderNumber = orderNumber;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.unixTimestamp = unixTimestamp;
        this.status = status;
        this.settlementReferenceNumber = settlementReferenceNumber;
        this.returnAuthCode = returnAuthCode;
    }

    public List<String> getFieldsForReturnAuthCodeValidation() {
        return Arrays.asList(
                orderNumber, paymentId, amount, currency, paymentMethod,
                Long.toString(unixTimestamp), status, settlementReferenceNumber);
    }

    public String formatToString() {
        return String.format("Received Paytrail %s response: ORDER_NUMBER=%s, PAYMENT_ID=%s, AMOUNT=%s, CURRENCY=%s, " +
                        "PAYMENT_METHOD=%s, TIMESTAMP=%d, STATUS=%s, SETTLEMENT_REFERENCE_NUMBER=%s, RETURN_AUTHCODE=%s",
                type.name(), orderNumber, paymentId, amount, currency, paymentMethod,
                unixTimestamp, status, settlementReferenceNumber, returnAuthCode);
    }

    public PaytrailPaymentEventType getType() {
        return type;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Long getUnixTimestamp() {
        return unixTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getSettlementReferenceNumber() {
        return settlementReferenceNumber;
    }

    public String getReturnAuthCode() {
        return returnAuthCode;
    }
}
