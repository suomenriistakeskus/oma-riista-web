package fi.riista.feature.common.entity;

public class GeoLocationInvalidException extends IllegalArgumentException {
    public GeoLocationInvalidException(final GeoLocation location) {
        super(String.format("Location is invalid lat=%d lng=%d",
                location.getLatitude(), location.getLongitude()));
    }
}
