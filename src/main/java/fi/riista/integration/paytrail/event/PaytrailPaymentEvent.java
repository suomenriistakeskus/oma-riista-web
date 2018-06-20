package fi.riista.integration.paytrail.event;

import fi.riista.feature.common.entity.BaseEntityEvent;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.validation.XssSafe;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class PaytrailPaymentEvent extends BaseEntityEvent {

    private Long id;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaytrailPaymentEventType eventType;

    @XssSafe
    @Size(max = 255)
    @Column
    private String remoteAddress;

    @XssSafe
    @Size(max = 255)
    @Column
    private String orderNumber;

    @XssSafe
    @Size(max = 255)
    @Column
    private String paymentId;

    @XssSafe
    @Size(max = 255)
    @Column
    private String status;

    @XssSafe
    @Size(max = 255)
    @Column
    private String currency;

    @XssSafe
    @Size(max = 255)
    @Column
    private String amount;

    @XssSafe
    @Size(max = 255)
    @Column
    private String paymentMethod;

    @XssSafe
    @Size(max = 255)
    @Column
    private String settlementReferenceNumber;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "paytrail_payment_event_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    PaytrailPaymentEvent() {
    }

    public PaytrailPaymentEvent(final PaytrailCallbackParameters callbackParameters) {
        super();
        setEventType(callbackParameters.getType());
        setStatus(callbackParameters.getStatus());
        setPaymentId(callbackParameters.getPaymentId());
        setPaymentMethod(callbackParameters.getPaymentMethod());
        setOrderNumber(callbackParameters.getOrderNumber());
        setAmount(callbackParameters.getAmount());
        setCurrency(callbackParameters.getCurrency());
        setSettlementReferenceNumber(callbackParameters.getSettlementReferenceNumber());
        setRemoteAddress(callbackParameters.getRemoteAddress());
    }

    public PaytrailPaymentEventType getEventType() {
        return eventType;
    }

    public void setEventType(final PaytrailPaymentEventType eventType) {
        this.eventType = eventType;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(final String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSettlementReferenceNumber() {
        return settlementReferenceNumber;
    }

    public void setSettlementReferenceNumber(final String settlementReferenceNumber) {
        this.settlementReferenceNumber = settlementReferenceNumber;
    }
}
