package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.GeoLocationInvalidException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestLocationSourceRequiredException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestLocationMutation implements HarvestMutation {

    public static HarvestLocationMutation createForMobile(final MobileHarvestDTO dto, final GeoLocation previousLocation) {
        return new HarvestLocationMutation(dto.getGeoLocation(), previousLocation,
                dto.getHarvestSpecVersion(), GeoLocation.Source.GPS_DEVICE);
    }

    public static HarvestLocationMutation createForWeb(final HarvestDTO dto, final GeoLocation previousLocation) {
        return new HarvestLocationMutation(dto.getGeoLocation(), previousLocation,
                HarvestSpecVersion.MOST_RECENT, GeoLocation.Source.MANUAL);
    }

    private final GeoLocation updatedLocation;
    private final boolean latLngModified;

    private HarvestLocationMutation(@Nonnull final GeoLocation dtoLocation,
                                    final GeoLocation currentLocation,
                                    @Nonnull final HarvestSpecVersion harvestSpecVersion,
                                    @Nonnull final GeoLocation.Source defaultLocationSource) {
        Objects.requireNonNull(dtoLocation);
        Objects.requireNonNull(harvestSpecVersion);
        Objects.requireNonNull(defaultLocationSource);

        if (dtoLocation.getLatitude() == 0 || dtoLocation.getLongitude() == 0) {
            // Check for default value for non-null field
            throw new GeoLocationInvalidException(dtoLocation);
        }

        if (currentLocation == null) {
            if (dtoLocation.getSource() == null) {
                // Use default GeoLocation.Source if not provided
                dtoLocation.setSource(defaultLocationSource);
            }
            this.updatedLocation = dtoLocation;

        } else if (dtoLocation.getSource() != null) {
            // Null-checking of geolocation source done for backwards-compatibility.
            this.updatedLocation = dtoLocation;

        } else {
            if (harvestSpecVersion.requiresGeolocationSource()) {
                throw new HarvestLocationSourceRequiredException();
            } else {
                // Keep original location within updates if source is missing from DTO.
                this.updatedLocation = currentLocation;
            }
        }

        this.latLngModified = updatedLocation.hasSameLatLng(currentLocation);
    }

    @Override
    public void accept(final Harvest harvest) {
        harvest.setGeoLocation(updatedLocation);
    }

    @Nonnull
    public GeoLocation getUpdatedLocation() {
        return updatedLocation.copy();
    }

    public boolean isLatLngModified() {
        return latLngModified;
    }
}
