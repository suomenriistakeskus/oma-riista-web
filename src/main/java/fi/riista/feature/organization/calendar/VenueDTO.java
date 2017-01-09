package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.entity.BaseEntityDTO;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class VenueDTO extends BaseEntityDTO<Long> {

    public static VenueDTO create(Venue venue) {
        VenueDTO dto = new VenueDTO();
        dto.setId(venue.getId());
        dto.setRev(venue.getConsistencyVersion());

        dto.setName(venue.getName());
        if (venue.getAddress() != null) {
            dto.setAddress(VenueAddressDTO.from(venue.getAddress()));
        }
        dto.setInfo(venue.getInfo());
        return dto;
    }

    private Long id;
    private Integer rev;

    @Size(max = 255) @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @Valid
    private VenueAddressDTO address;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String info;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VenueAddressDTO getAddress() {
        return address;
    }

    public void setAddress(VenueAddressDTO address) {
        this.address = address;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
