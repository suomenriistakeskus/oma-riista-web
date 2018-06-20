package fi.riista.integration.paytrail.e2.model;

import fi.riista.util.BigDecimalMoney;
import fi.riista.validation.FinnishCreditorReference;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Payment {
    // BASIC DETAILS

    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = "[0-9a-zA-Z()\\[\\]{}*+\\-_,. ]{1,64}")
    private String orderNumber;

    @FinnishCreditorReference
    private String referenceNumber;

    private BigDecimalMoney amount;

    @Pattern(regexp = "EUR")
    private String currency;

    @Valid
    private final PayerPerson payerPerson = new PayerPerson();

    @Valid
    private final List<Product> products = new LinkedList<>();

    @Length(max = 64)
    @Pattern(regexp = "[0-9,]{0,64}")
    private String paymentMethods;
    private Boolean vatIsIncluded;
    private Locale locale;

    @Length(max = 255)
    @Pattern(regexp = "[\\pL-0-9- \"', ()\\[\\]{}*+\\-_,.]*")
    private String msgUiPaymentMethod;

    @Length(max = 255)
    @Pattern(regexp = "[\\pL-0-9- \"', ()\\[\\]{}*+\\-_,.]*")
    private String msgUiMerchantPanel;

    @Length(max = 255)
    @Pattern(regexp = "[\\pL-0-9- \"', ()\\[\\]{}*+\\-_,.]*")
    private String msgSettlementPayer;

    @Length(max = 255)
    @Pattern(regexp = "[\\pL-0-9- \"', ()\\[\\]{}*+\\-_,.]*")
    private String msgSettlementMerchant;

    // CALLBACKS

    @NotNull
    private URI successUri;

    @NotNull
    private URI cancelUri;

    private URI notifyUri;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(final String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimalMoney getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimalMoney amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public String getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(final String paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public Boolean getVatIsIncluded() {
        return vatIsIncluded;
    }

    public void setVatIsIncluded(final Boolean vatIsIncluded) {
        this.vatIsIncluded = vatIsIncluded;
    }

    public URI getSuccessUri() {
        return successUri;
    }

    public void setSuccessUri(final URI successUri) {
        this.successUri = successUri;
    }

    public URI getCancelUri() {
        return cancelUri;
    }

    public void setCancelUri(final URI cancelUri) {
        this.cancelUri = cancelUri;
    }

    public URI getNotifyUri() {
        return notifyUri;
    }

    public void setNotifyUri(final URI notifyUri) {
        this.notifyUri = notifyUri;
    }

    public List<Product> getProducts() {
        return products;
    }

    public PayerPerson getPayerPerson() {
        return payerPerson;
    }

    public String getMsgSettlementPayer() {
        return msgSettlementPayer;
    }

    public void setMsgSettlementPayer(final String msgSettlementPayer) {
        this.msgSettlementPayer = msgSettlementPayer;
    }

    public String getMsgSettlementMerchant() {
        return msgSettlementMerchant;
    }

    public void setMsgSettlementMerchant(final String msgSettlementMerchant) {
        this.msgSettlementMerchant = msgSettlementMerchant;
    }

    public String getMsgUiPaymentMethod() {
        return msgUiPaymentMethod;
    }

    public void setMsgUiPaymentMethod(final String msgUiPaymentMethod) {
        this.msgUiPaymentMethod = msgUiPaymentMethod;
    }

    public String getMsgUiMerchantPanel() {
        return msgUiMerchantPanel;
    }

    public void setMsgUiMerchantPanel(final String msgUiMerchantPanel) {
        this.msgUiMerchantPanel = msgUiMerchantPanel;
    }
}
