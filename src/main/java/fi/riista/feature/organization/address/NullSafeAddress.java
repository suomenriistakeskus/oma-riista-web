package fi.riista.feature.organization.address;

import javax.annotation.Nullable;

public class NullSafeAddress {

    private final Address address;

    private NullSafeAddress(@Nullable Address address) {
        this.address = address;
    }

    public static NullSafeAddress of(@Nullable Address address) {
        return new NullSafeAddress(address);
    }

    public String getStreetAddress() {
        return address != null ? address.getStreetAddress() : null;
    }

    public String getPostalCode() {
        return address != null ? address.getPostalCode() : null;
    }

    public String getCity() {
        return address != null ? address.getCity(): null;
    }

    public String getCountry(){
        return address != null ? address.getCountry(): null;
    }
}
