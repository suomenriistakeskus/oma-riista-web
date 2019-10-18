package fi.riista.feature.gis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class GISWGS84Point implements Serializable {
    
    @JsonCreator
    public static GISWGS84Point create(@JsonProperty("latitude") final double latitude,
                                       @JsonProperty("longitude") final double longitude) {
        return new GISWGS84Point(latitude, longitude);
    }

    private final double latitude;
    private final double longitude;

    public GISWGS84Point(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toWellKnownText() {
        return "POINT(" + getLongitude() + " " + getLatitude() + ")";
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "GISWGS84Point{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
