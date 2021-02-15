package fi.riista.feature.huntingclub;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;

public class CreateHuntingClubDTO {

    // If moderator is creating the club, then here is id of the person who is the contact person.
    // If person himself is creating the club, then this should be null and ignored.
    private Long personId;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Valid
    @NotNull
    private GeoLocation geoLocation;

    public HuntingClubDTO toHuntingClubDTO() {
        final HuntingClubDTO clubDto = new HuntingClubDTO();
        clubDto.setNameFI(nameFI);
        clubDto.setNameSV(nameSV);
        clubDto.setGeoLocation(geoLocation);
        if (personId != null) {
            // When moderator is creating, use this one occupation to transfer the person id, to who the club is created
            final OccupationDTO o = new OccupationDTO();
            o.setPersonId(personId);
            clubDto.setYhdyshenkilot(Collections.singletonList(o));
        }
        return clubDto;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(final Long personId) {
        this.personId = personId;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(String nameSV) {
        this.nameSV = nameSV;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
