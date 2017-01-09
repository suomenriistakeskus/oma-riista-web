package fi.riista.feature.organization.address;

import com.google.common.base.MoreObjects;
import fi.riista.feature.common.entity.LifecycleEntity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class Address extends LifecycleEntity<Long> {

    private Long id;

    @Size(max = 255)
    @Column
    private String streetAddress;

    @Size(max = 255)
    @Column
    private String city;

    @Size(max = 255)
    @Column
    private String postalCode;

    @Size(max = 255)
    @Column(name = "country_name")
    private String country;

    @Size(min = 2, max = 2)
    @Column(length = 2)
    private String countryCode;

    public Address() {
    }

    public Address(String streetAddress, String postalCode, String city, String country) {
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "address_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(final String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("streetAddress", streetAddress)
                .add("city", city)
                .add("postalCode", postalCode)
                .add("country", country)
                .add("countryCode", countryCode)
                .toString();
    }
}
