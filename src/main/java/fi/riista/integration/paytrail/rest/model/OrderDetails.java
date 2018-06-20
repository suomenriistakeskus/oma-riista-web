package fi.riista.integration.paytrail.rest.model;

import fi.riista.integration.support.BooleanOneZeroAdapter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "includeVat",
        "contact",
        "products"
})
public class OrderDetails {

    @XmlElement(name = "includeVat", required = true)
    @XmlJavaTypeAdapter(BooleanOneZeroAdapter.class)
    private Boolean includeVat;

    @Valid
    @NotNull
    @XmlElement(name = "contact", required = true)
    private Contact contact;

    @Valid
    @NotNull
    @XmlElement(name = "products", required = true)
    private ProductList products;

    public Boolean getIncludeVat() {
        return includeVat;
    }

    public void setIncludeVat(final Boolean includeVat) {
        this.includeVat = includeVat;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(final Contact contact) {
        this.contact = contact;
    }

    public ProductList getProducts() {
        return products;
    }

    public void setProducts(final ProductList products) {
        this.products = products;
    }
}
