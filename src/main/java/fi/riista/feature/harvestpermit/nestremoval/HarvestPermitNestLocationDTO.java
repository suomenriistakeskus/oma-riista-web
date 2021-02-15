package fi.riista.feature.harvestpermit.nestremoval;

import fi.riista.feature.common.entity.GeoLocation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HarvestPermitNestLocationDTO {

    @Valid
    private GeoLocation geoLocation;

    @Valid
    @NotNull
    private HarvestPermitNestLocationType nestLocationType;

    public HarvestPermitNestLocationDTO() {}

    public HarvestPermitNestLocationDTO(final GeoLocation geoLocation, final HarvestPermitNestLocationType nestLocationType) {
        this.geoLocation = geoLocation;
        this.nestLocationType = nestLocationType;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public HarvestPermitNestLocationType getNestLocationType() {
        return nestLocationType;
    }

    public void setNestLocationType(final HarvestPermitNestLocationType nestLocationType) {
        this.nestLocationType = nestLocationType;
    }
}
