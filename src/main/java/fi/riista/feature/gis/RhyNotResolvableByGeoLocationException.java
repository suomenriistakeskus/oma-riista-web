package fi.riista.feature.gis;

import fi.riista.feature.common.entity.GeoLocation;

public class RhyNotResolvableByGeoLocationException extends RuntimeException {
    private final int longitude;
    private final int latitude;

    public RhyNotResolvableByGeoLocationException(final GeoLocation geoLocation) {
        super(String.format("Could not resolve RHY for lat=%d lng=%d",
                geoLocation.getLatitude(),
                geoLocation.getLongitude()));
        this.longitude = geoLocation.getLongitude();
        this.latitude = geoLocation.getLatitude();
    }

    public int getLongitude() {
        return longitude;
    }

    public int getLatitude() {
        return latitude;
    }
}
