package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.common.entity.GeoLocation;

import javax.validation.Valid;

public class PermitUsageLocationDTO {

    public static PermitUsageLocationDTO create(final PermitUsageLocation location) {
        return new PermitUsageLocationDTO(location.getGeoLocation());
    }

    @Valid
    private GeoLocation geoLocation;

    public PermitUsageLocationDTO() {
    }

    public PermitUsageLocationDTO(final @Valid GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
