package fi.riista.integration.paytrail.e2.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

public class PayerPerson {
    @Pattern(regexp = "[0-9+-]{0,64}")
    private String phoneNumber;

    @Email
    private String email;

    @Length(max = 64)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String firstName;

    @Length(max = 64)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String lastName;

    @Length(max = 128)
    private String streetAddress;

    @Length(max = 16)
    @Pattern(regexp = "[0-9a-zA-Z]{0,16}")
    private String postalCode;

    @Length(max = 64)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String town;

    @Length(max = 2)
    @Pattern(regexp = "[a-zA-Z]{0,2}")
    private String country;

    @Length(max = 128)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String companyName;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(final String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getTown() {
        return town;
    }

    public void setTown(final String town) {
        this.town = town;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }
}
