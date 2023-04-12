package fi.riista.integration.paytrail.checkout.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

public class Address {

    @Length(max = 128)
    private String streetAddress;

    @Length(max = 16)
    @Pattern(regexp = "[0-9a-zA-Z]{0,16}")
    private String postalCode;

    @Length(max = 64)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String city;

    @Length(max = 2)
    @Pattern(regexp = "[a-zA-Z]{0,2}")
    private String country;

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

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

}
