package fi.riista.integration.paytrail.rest.model;

import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "telephone",
        "mobile",
        "email",
        "firstName",
        "lastName",
        "companyName",
        "address"
})
public class Contact {

    @PhoneNumber
    @Length(max = 64)
    @XmlElement(name = "telephone")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String telephone;

    @PhoneNumber
    @Length(max = 64)
    @XmlElement(name = "mobile")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String mobile;

    @Email
    @NotBlank
    @Length(max = 255)
    @XmlElement(name = "email", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String email;

    @NotBlank
    @Length(max = 64)
    @ValidPaytrailRestString
    @XmlElement(name = "firstName", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String firstName;

    @NotBlank
    @Length(max = 64)
    @ValidPaytrailRestString
    @XmlElement(name = "lastName", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String lastName;

    @Length(max = 64)
    @XmlElement(name = "companyName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String companyName;

    @NotNull
    @Valid
    @XmlElement(name = "address", required = true)
    private ContactAddress address;

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(final String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }

    public ContactAddress getAddress() {
        return address;
    }

    public void setAddress(final ContactAddress address) {
        this.address = address;
    }
}
