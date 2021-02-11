package fi.riista.feature.common.entity;

public class InvalidGeoLocationException extends IllegalArgumentException {
    public InvalidGeoLocationException(final GeoLocation location) {
        super(String.format("Location is invalid lat=%d lng=%d",
                location.getLatitude(), location.getLongitude()));
    }
}
