package fi.riista.feature.common.entity;

import com.google.common.base.MoreObjects;
import com.vividsolutions.jts.geom.Coordinate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

@Embeddable
@Access(AccessType.FIELD)
public class GeoLocation implements Serializable {

    public enum Source {
        GPS_DEVICE, MANUAL
    }

    public static final Function<GeoLocation, Coordinate> TO_COORDINATE =
            location -> location == null ? null : location.toCoordinate();

    @Column(nullable = false)
    private int latitude;

    @Column(nullable = false)
    private int longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "geolocation_source")
    private Source source;

    private Double accuracy;

    private Double altitude;

    private Double altitudeAccuracy;

    public GeoLocation() {
    }

    public GeoLocation(final int latitude, final int longitude) {
        this(latitude, longitude, Source.MANUAL);
    }

    public GeoLocation(final int latitude, final int longitude, final Source source) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.source = source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, source, accuracy, altitude, altitudeAccuracy);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeoLocation that = (GeoLocation) obj;

        return this.latitude == that.latitude
                && this.longitude == that.longitude
                && Objects.equals(this.source, that.source)
                && Objects.equals(this.accuracy, that.accuracy)
                && Objects.equals(this.altitude, that.altitude)
                && Objects.equals(this.altitudeAccuracy, that.altitudeAccuracy);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .toString();
    }

    public GeoLocation copy() {
        return withLatLng(this.latitude, this.longitude);
    }

    public GeoLocation move(final int latitudeOffset, final int longitudeOffset) {
        return withLatLng(this.latitude + latitudeOffset, this.longitude + longitudeOffset);
    }

    public GeoLocation withLatLng(final int latitude, final int longitude) {
        final GeoLocation that = new GeoLocation();
        that.setLatitude(latitude);
        that.setLongitude(longitude);
        that.setSource(this.source);
        that.setAccuracy(this.accuracy);
        that.setAltitude(this.altitude);
        that.setAltitudeAccuracy(this.altitudeAccuracy);
        return that;
    }

    public GeoLocation withSource(final Source source) {
        final GeoLocation that = copy();
        that.setSource(source);
        return that;
    }

    public Coordinate toCoordinate() {
        return new Coordinate(longitude, latitude);
    }

    // Accessors -->

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(final int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(final int longitude) {
        this.longitude = longitude;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(final Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(final Double altitude) {
        this.altitude = altitude;
    }

    public Double getAltitudeAccuracy() {
        return altitudeAccuracy;
    }

    public void setAltitudeAccuracy(final Double altitudeAccuracy) {
        this.altitudeAccuracy = altitudeAccuracy;
    }

}
