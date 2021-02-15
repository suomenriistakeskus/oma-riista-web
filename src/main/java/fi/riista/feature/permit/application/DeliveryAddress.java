package fi.riista.feature.permit.application;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Embeddable
@Access(value = AccessType.FIELD)
public class DeliveryAddress {

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "delivery_address_recipient", nullable = false)
    private String recipient;

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "delivery_address_street_address", nullable = false)
    private String streetAddress;

    @Pattern(regexp = "[0-9]{1,10}")
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "delivery_address_postal_code", nullable = false)
    private String postalCode;

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "delivery_address_city", nullable = false)
    private String city;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "delivery_address_country_name")
    private String country;

    public static DeliveryAddress create(@Nonnull final String recipient,
                                         @Nonnull final Address address) {
        requireNonNull(recipient);
        requireNonNull(address);

        final DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setRecipient(recipient);
        deliveryAddress.setStreetAddress(address.getStreetAddress());
        deliveryAddress.setPostalCode(address.getPostalCode());
        deliveryAddress.setCity(address.getCity());
        deliveryAddress.setCountry(address.getCountry());
        return deliveryAddress;
    }

    public static DeliveryAddress createFromPersonNullable(@Nonnull final Person contactPerson) {
        requireNonNull(contactPerson);

        return Optional.ofNullable(contactPerson.getAddress()).map(address -> {
            final DeliveryAddress deliveryAddress = new DeliveryAddress();
            deliveryAddress.setRecipient(contactPerson.getFullName());
            deliveryAddress.setStreetAddress(address.getStreetAddress());
            deliveryAddress.setPostalCode(address.getPostalCode());
            deliveryAddress.setCity(address.getCity());
            deliveryAddress.setCountry(address.getCountry());
            return deliveryAddress;
        }).orElse(null);
    }

    public Address toAddress() {
        final Address address = new Address();
        address.setStreetAddress(getStreetAddress());
        address.setPostalCode(getPostalCode());
        address.setCity(getCity());
        address.setCountry(getCountry());

        return address;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

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
