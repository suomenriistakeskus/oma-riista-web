package fi.riista.feature.organization.calendar;

import com.google.common.base.MoreObjects;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.address.Address;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.Objects;

public class VenueAddressDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String streetAddress;

    @Length(max = 10)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String postalCode;

    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String city;

    public VenueAddressDTO() {
    }

    public VenueAddressDTO(Address address) {
        Objects.requireNonNull(address, "address must not be null");

        setId(address.getId());
        setRev(address.getConsistencyVersion());
        setStreetAddress(address.getStreetAddress());
        setPostalCode(address.getPostalCode());
        setCity(address.getCity());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final VenueAddressDTO dto = (VenueAddressDTO) o;

        return new EqualsBuilder()
                .append(id, dto.id)
                .append(rev, dto.rev)
                .append(streetAddress, dto.streetAddress)
                .append(postalCode, dto.postalCode)
                .append(city, dto.city)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(rev)
                .append(streetAddress)
                .append(postalCode)
                .append(city)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("rev", rev)
                .add("streetAddress", streetAddress)
                .add("postalCode", postalCode)
                .add("city", city)
                .toString();
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
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
}
