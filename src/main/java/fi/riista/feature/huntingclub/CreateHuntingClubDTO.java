package fi.riista.feature.huntingclub;

import fi.riista.feature.common.entity.GeoLocation;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class CreateHuntingClubDTO {

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @NotNull
    private GeoLocation geoLocation;

    public HuntingClubDTO toHuntingClubDTO() {
        final HuntingClubDTO clubDto = new HuntingClubDTO();
        clubDto.setNameFI(nameFI);
        clubDto.setNameSV(nameSV);
        clubDto.setGeoLocation(geoLocation);
        return clubDto;
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
