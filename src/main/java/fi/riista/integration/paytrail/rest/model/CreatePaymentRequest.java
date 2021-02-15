package fi.riista.integration.paytrail.rest.model;

import fi.riista.integration.support.TwoDecimalBigDecimalAdapter;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "orderNumber",
        "referenceNumber",
        "description",
        "currency",
        "locale",
        "urlSet",
        "price",
        "orderDetails"
})
@XmlRootElement(name = "payment")
public class CreatePaymentRequest {

    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = "^[0-9a-zA-Z()\\[\\]{}*+\\-_,. ]{1,64}$")
    @XmlElement(name = "orderNumber", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String orderNumber;

    @Length(max = 22)
    @XmlElement(name = "referenceNumber")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String referenceNumber;

    @Length(max = 65000)
    @XmlElement(name = "description")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String description;

    @NotBlank
    @Length(min = 3, max = 3)
    @XmlElement(name = "currency", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String currency;

    @Pattern(regexp = "^[a-z]{1,2}[_][A-Z]{1,2}$")
    @XmlElement(name = "locale")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String locale;

    @NotNull
    @Valid
    @XmlElement(name = "urlSet")
    private UrlSet urlSet;

    @DecimalMin("0.65")
    @DecimalMax("499999")
    @XmlElement(name = "price")
    @XmlJavaTypeAdapter(TwoDecimalBigDecimalAdapter.class)
    private BigDecimal price;

    @Valid
    @XmlElement(name = "orderDetails")
    private OrderDetails orderDetails;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public UrlSet getUrlSet() {
        return urlSet;
    }

    public void setUrlSet(final UrlSet urlSet) {
        this.urlSet = urlSet;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(final OrderDetails orderDetails) {
        this.orderDetails = orderDetails;
    }
}
