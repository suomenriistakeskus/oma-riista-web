package fi.riista.feature.permit.application.derogation.area;

import fi.riista.feature.common.entity.GeoLocation;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class DerogationPermitApplicationAreaDTO {

    @NotNull
    @Min(1)
    private Integer areaSize;

    @Valid
    @NotNull
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String areaDescription;

    public static DerogationPermitApplicationAreaDTO createFrom(@Nonnull final DerogationPermitApplicationAreaInfo derogationPermitApplication) {
        requireNonNull(derogationPermitApplication);
        return new DerogationPermitApplicationAreaDTO(derogationPermitApplication.getAreaSize(),
                derogationPermitApplication.getGeoLocation(), derogationPermitApplication.getAreaDescription());
    }

    private DerogationPermitApplicationAreaDTO(final Integer areaSize, final GeoLocation geoLocation,
                                               final String areaDescription) {
        this.areaSize = areaSize;
        this.geoLocation = geoLocation;
        this.areaDescription = areaDescription;
    }

    public DerogationPermitApplicationAreaDTO() {

    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public String getAreaDescription() {
        return areaDescription;
    }
}
