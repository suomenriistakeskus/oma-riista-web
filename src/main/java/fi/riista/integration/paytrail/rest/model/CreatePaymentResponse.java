package fi.riista.integration.paytrail.rest.model;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "orderNumber",
        "token",
        "url"
})
@XmlRootElement(name = "payment")
public class CreatePaymentResponse {

    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = "^[0-9a-zA-Z()\\[\\]{}*+\\-_,. ]{1,64}$")
    @XmlElement(name = "orderNumber", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String orderNumber;

    @XmlElement(name = "token", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String token;

    @XmlElement(name = "url", required = true)
    private URI url;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(final URI url) {
        this.url = url;
    }
}
