package fi.riista.feature.harvestregistry;

import java.util.Optional;

public class HarvestRegistryShooterAddressDTO {

    private final String streetAddress;
    private final String postalCode;
    private final String city;
    private final String country;

    public static HarvestRegistryShooterAddressDTO createFrom(final HarvestRegistryItem item) {
        return Optional.ofNullable(item.getShooterAddress())
                .map(address ->
                        new HarvestRegistryShooterAddressDTO(
                                address.getStreetAddress(),
                                address.getPostalCode(),
                                address.getCity(),
                                address.getCountry())
                ).orElse(null);

    }

    private HarvestRegistryShooterAddressDTO(final String streetAddress, final String postalCode, final String city,
                                             final String country) {
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
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

    public String formatToString() {
        return streetAddress + "\n" +
                postalCode + " " + city + "\n" +
                country;
    }
}
