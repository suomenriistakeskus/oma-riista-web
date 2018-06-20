package fi.riista.integration.paytrail.rest.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "street",
        "postalCode",
        "postalOffice",
        "country"
})
public class ContactAddress {

    @Length(max = 128)
    @ValidPaytrailRestString
    @XmlElement(name = "street", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String street;

    @Length(max = 16)
    @XmlElement(name = "postalCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String postalCode;

    @Length(max = 64)
    @Pattern(regexp = "^[0-9a-zA-Z]*$")
    @XmlElement(name = "postalOffice", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String postalOffice;

    @Length(min = 2, max = 2)
    @Pattern(regexp = "^[a-zA-Z]*$")
    @XmlElement(name = "country", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String country;

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostalOffice() {
        return postalOffice;
    }

    public void setPostalOffice(final String postalOffice) {
        this.postalOffice = postalOffice;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }
}
