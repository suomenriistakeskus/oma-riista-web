package fi.riista.integration.paytrail.checkout.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.validation.FinnishCreditorReference;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.LinkedList;
import java.util.List;

public class Payment {

    // BASIC DETAILS

    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = "[0-9a-zA-Z()\\[\\]{}*+\\-_,. ]{1,64}")
    private String stamp;

    @FinnishCreditorReference
    private String reference;

    private Integer amount;

    @Pattern(regexp = "EUR")
    private String currency;

    private PaytrailLocale language;

    @Valid
    private final List<Item> items = new LinkedList<>();

    @Valid
    private final Customer customer = new Customer();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Address deliveryAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Address invoicingAddress;

    private CallbackUrl redirectUrls;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CallbackUrl callbackUrls;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer callbackDelay;

    public String getStamp() {
        return stamp;
    }

    public void setStamp(final String stamp) {
        this.stamp = stamp;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public PaytrailLocale getLanguage() {
        return language;
    }

    public void setLanguage(final PaytrailLocale language) {
        this.language = language;
    }

    public List<Item> getItems() {
        return items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(final Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Address getInvoicingAddress() {
        return invoicingAddress;
    }

    public void setInvoicingAddress(final Address invoicingAddress) {
        this.invoicingAddress = invoicingAddress;
    }

    public CallbackUrl getRedirectUrls() {
        return redirectUrls;
    }

    public void setRedirectUrls(final CallbackUrl redirectUrls) {
        this.redirectUrls = redirectUrls;
    }

    public CallbackUrl getCallbackUrls() {
        return callbackUrls;
    }

    public void setCallbackUrls(final CallbackUrl callbackUrls) {
        this.callbackUrls = callbackUrls;
    }

    public Integer getCallbackDelay() {
        return callbackDelay;
    }

    public void setCallbackDelay(final Integer callbackDelay) {
        this.callbackDelay = callbackDelay;
    }
}
