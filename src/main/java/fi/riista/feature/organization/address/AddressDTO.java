package fi.riista.feature.organization.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import fi.riista.feature.common.dto.BaseEntityDTO;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import java.util.Objects;

public class AddressDTO extends BaseEntityDTO<Long> {

    public static AddressDTO from(Address address) {
        return address != null ? new AddressDTO(address) : null;
    }

    private Long id;
    private Integer rev;

    private boolean editable;

    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String streetAddress;

    @Length(max = 10)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String postalCode;

    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String city;

    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String country;

    public AddressDTO() {
    }

    public AddressDTO(Address address) {
        Objects.requireNonNull(address, "address must not be null");

        setId(address.getId());
        setRev(address.getConsistencyVersion());
        setStreetAddress(address.getStreetAddress());
        setPostalCode(address.getPostalCode());
        setCity(address.getCity());

        if (StringUtils.hasText(address.getCountry())) {
            setCountry(address.getCountry());
        } else {
            setCountry(address.getCountryCode());
        }
    }

    @AssertTrue
    @JsonIgnore
    public boolean isValidAddress() {
        return StringUtils.hasText(streetAddress)
                && StringUtils.hasText(postalCode)
                && StringUtils.hasText(city);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AddressDTO dto = (AddressDTO) o;

        return new EqualsBuilder()
                .append(id, dto.id)
                .append(rev, dto.rev)
                .append(editable, dto.editable)
                .append(streetAddress, dto.streetAddress)
                .append(postalCode, dto.postalCode)
                .append(city, dto.city)
                .append(country, dto.country)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(rev)
                .append(editable)
                .append(streetAddress)
                .append(postalCode)
                .append(city)
                .append(country)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("rev", rev)
                .add("editable", editable)
                .add("streetAddress", streetAddress)
                .add("postalCode", postalCode)
                .add("city", city)
                .add("country", country)
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

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
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
