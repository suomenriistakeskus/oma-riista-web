package fi.riista.feature.account.registration;

import fi.riista.feature.common.dto.XssSafe;
import fi.riista.validation.PhoneNumber;
import fi.riista.validation.VetumaTransactionId;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class CompleteRegistrationDTO implements Serializable {
    @XssSafe
    @VetumaTransactionId
    private String trid;

    @XssSafe
    @NotBlank
    @Size(min = 8)
    private String password;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String byName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @NotBlank
    @PhoneNumber
    private String phoneNumber;

    @NotNull
    @Size(min = 2, max = 2)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lang;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String ssn;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String streetAddress;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String city;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String postalCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String country;

    private boolean addressEditable;

    public String getTrid() {
        return trid;
    }

    public void setTrid(final String trid) {
        this.trid = trid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(String byName) {
        this.byName = byName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setAddressEditable(boolean addressEditable) {
        this.addressEditable = addressEditable;
    }

    public boolean isAddressEditable() {
        return addressEditable;
    }
}
