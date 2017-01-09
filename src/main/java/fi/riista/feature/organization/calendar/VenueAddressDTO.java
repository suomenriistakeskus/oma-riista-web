package fi.riista.feature.organization.calendar;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.Address;

import javax.validation.constraints.AssertTrue;

public class VenueAddressDTO extends AddressDTO {

    public static VenueAddressDTO from(Address address) {
        return address != null ? new VenueAddressDTO(address) : null;
    }

    public VenueAddressDTO() {
        super();
    }

    public VenueAddressDTO(Address address) {
        super(address);
    }

    @AssertTrue
    @JsonIgnore
    @Override
    public boolean isValidAddress() {
        return true;
    }

}
