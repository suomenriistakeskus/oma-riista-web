package fi.riista.feature.permit.application.carnivore.area;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class CarnivorePermitApplicationAreaDTO {

    @NotNull
    @Min(1)
    private Integer areaSize;

    @Valid
    @NotNull
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String areaDescription;

    public static CarnivorePermitApplicationAreaDTO createFrom(@Nonnull final CarnivorePermitApplication carnivorePermitApplication) {
        requireNonNull(carnivorePermitApplication);
        return new CarnivorePermitApplicationAreaDTO(carnivorePermitApplication.getAreaSize(),
                carnivorePermitApplication.getGeoLocation(), carnivorePermitApplication.getAreaDescription());
    }

    private CarnivorePermitApplicationAreaDTO(final Integer areaSize, final GeoLocation geoLocation,
                                              final String areaDescription) {
        this.areaSize = areaSize;
        this.geoLocation = geoLocation;
        this.areaDescription = areaDescription;
    }

    public CarnivorePermitApplicationAreaDTO() {

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
