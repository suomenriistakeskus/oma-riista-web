package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.InvalidGeoLocationException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestLocationSourceRequiredException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class HarvestLocationMutation implements HarvestMutation {

    public static HarvestLocationMutation createForMobile(final MobileHarvestDTO dto, final GeoLocation previousLocation) {
        return new HarvestLocationMutation(dto.getGeoLocation(), previousLocation);
    }

    public static HarvestLocationMutation createForWeb(final HarvestDTO dto, final GeoLocation previousLocation) {
        final GeoLocation geoLocation = dto.getGeoLocation();
        final GeoLocation geoLocationWithSource = geoLocation.getSource() != null
                ? geoLocation
                : geoLocation.withSource(GeoLocation.Source.MANUAL);
        return new HarvestLocationMutation(geoLocationWithSource, previousLocation);
    }

    private final GeoLocation updatedLocation;
    private final boolean latLngModified;

    private HarvestLocationMutation(@Nonnull final GeoLocation dtoLocation, final GeoLocation previousLocation) {
        requireNonNull(dtoLocation);
        requireNonNull(dtoLocation.getSource());

        if (dtoLocation.getLatitude() == 0 || dtoLocation.getLongitude() == 0) {
            // Check for default value for non-null field.
            throw new InvalidGeoLocationException(dtoLocation);
        }

        // Ensure invariant holds because some historical mobile app versions did not support geolocation source.
        if (dtoLocation.getSource() == null) {
            throw new HarvestLocationSourceRequiredException();
        }

        this.updatedLocation = dtoLocation;
        this.latLngModified = updatedLocation.hasSameLatLng(previousLocation);
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
