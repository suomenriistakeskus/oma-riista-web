package fi.riista.integration.paytrail.checkout.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;

public class PaytrailPaymentInitResponse {

    private String transactionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String href;

    private String terms;

    private List<PaymentMethodGroupData> groups;

    private String reference;

    private List<Provider> providers;
    private Object customProviders;

    @Valid
    private final List<Item> item = new LinkedList<>();

    @Valid
    private final Customer customer = new Customer();

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    public String getHref() {
        return href;
    }

    public void setHref(final String href) {
        this.href = href;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(final String terms) {
        this.terms = terms;
    }

    public List<PaymentMethodGroupData> getGroups() {
        return groups;
    }

    public void setGroups(final List<PaymentMethodGroupData> groups) {
        this.groups = groups;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public Object getCustomProviders() {
        return customProviders;
    }

    public void setCustomProviders(final Object customProviders) {
        this.customProviders = customProviders;
    }

    public void setProviders(final List<Provider> providers) {
        this.providers = providers;
    }

    public List<Item> getItem() {
        return item;
    }

    public Customer getCustomer() {
        return customer;
    }
}
