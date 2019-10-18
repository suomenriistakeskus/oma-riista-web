package fi.riista.feature.harvestregistry;

import fi.riista.feature.organization.address.Address;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.util.Optional;

@Embeddable
@Access(value = AccessType.FIELD)
public class ShooterAddress {

    public static ShooterAddress createFrom(final Address address) {

        return Optional.ofNullable(address).map(a -> {
            final ShooterAddress shooterAddress = new ShooterAddress();
            shooterAddress.setStreetAddress(a.getStreetAddress());
            shooterAddress.setPostalCode(a.getPostalCode());
            shooterAddress.setCity(a.getCity());
            shooterAddress.setCountry(a.getCountry());
            return shooterAddress;
        }).orElse(null);
    }

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "shooter_address_street_address", nullable = false)
    private String streetAddress;

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "shooter_address_postal_code", nullable = false)
    private String postalCode;

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "shooter_address_city", nullable = false)
    private String city;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "shooter_address_country")
    private String country;

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
