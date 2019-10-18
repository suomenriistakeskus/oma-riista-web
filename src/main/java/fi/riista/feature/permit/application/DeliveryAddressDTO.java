package fi.riista.feature.permit.application;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DeliveryAddressDTO {

    public static DeliveryAddressDTO fromNullable(DeliveryAddress deliveryAddress) {
        return Optional.ofNullable(deliveryAddress).map(DeliveryAddressDTO::from)
                .orElse(null);
    }

    public static DeliveryAddressDTO from(@Nonnull DeliveryAddress deliveryAddress) {
        requireNonNull(deliveryAddress);

        return new DeliveryAddressDTO(
                deliveryAddress.getRecipient(),
                deliveryAddress.getStreetAddress(),
                deliveryAddress.getPostalCode(),
                deliveryAddress.getCity(),
                deliveryAddress.getCountry());
    }

    public DeliveryAddress toEntity() {
        final DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setRecipient(this.getRecipient());
        deliveryAddress.setStreetAddress(this.getStreetAddress());
        deliveryAddress.setPostalCode(this.getPostalCode());
        deliveryAddress.setCity(this.getCity());
        deliveryAddress.setCountry(this.getCountry());
        return deliveryAddress;
    }

    @SafeHtml
    @Length(min = 1, max = 255)
    private String recipient;

    @SafeHtml
    @Length(min = 1, max = 255)
    private String streetAddress;

    @SafeHtml
    @Pattern(regexp = "[0-9]{1,10}")
    private String postalCode;

    @SafeHtml
    @Length(min = 1, max = 255)
    private String city;

    @SafeHtml
    @Length(min = 1, max = 255)
    private String country;

    public DeliveryAddressDTO() {
    }

    private DeliveryAddressDTO(@Nonnull String recipient,
                               @Nonnull String streetAddress,
                               @Nonnull String postalCode,
                               @Nonnull String city,
                               @Nullable String country) {
        this.recipient = requireNonNull(recipient);
        this.streetAddress = requireNonNull(streetAddress);
        this.postalCode = requireNonNull(postalCode);
        this.city = requireNonNull(city);
        this.country = country;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
