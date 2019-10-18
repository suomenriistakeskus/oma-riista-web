package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Objects;

public class VenueDTO extends BaseEntityDTO<Long> {

    public static VenueDTO create(final Venue venue, final Address venueAddress) {
        final VenueDTO dto = new VenueDTO();
        DtoUtil.copyBaseFields(venue, dto);

        dto.setName(venue.getName());
        dto.setInfo(venue.getInfo());

        final VenueAddressDTO address = new VenueAddressDTO(venueAddress);
        dto.setAddress(address);

        return dto;
    }

    private Long id;
    private Integer rev;

    @Size(max = 255)
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @Valid
    private VenueAddressDTO address;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String info;

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof VenueDTO)) {
            return false;
        } else {
            final VenueDTO that = (VenueDTO) o;

            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.rev, that.rev)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.info, that.info)
                    && Objects.equals(this.address, that.address);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rev, name, info, address);
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public VenueAddressDTO getAddress() {
        return address;
    }

    public void setAddress(final VenueAddressDTO address) {
        this.address = address;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(final String info) {
        this.info = info;
    }
}
