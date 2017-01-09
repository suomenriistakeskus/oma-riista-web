package fi.riista.feature.gis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.mml.support.WFSUtil;

import java.io.Serializable;

public class GISPoint implements Serializable {
    @JsonCreator
    public static GISPoint create(@JsonProperty("lat") double latitude,
                                  @JsonProperty("lon") double longitude) {
        return new GISPoint((int) Math.round(latitude), (int) Math.round(longitude));
    }

    public static GISPoint create(final GeoLocation geoLocation) {
        return new GISPoint(geoLocation.getLatitude(), geoLocation.getLongitude());
    }

    /**
     * Coordinates are always expected to use ETRS-TM35FIN projection for GIS
     */
    private int latitude;
    private int longitude;

    public GISPoint(int latitude, int longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toWellKnownText() {
        return "POINT(" + getLongitude() + " " + getLatitude() + ")";
    }

    public String toPointString() {
        return getLongitude() + " " + getLatitude();
    }

    public String toGmlPoint() {
        return "<gml:Point srsName=\"" + WFSUtil.SRS_NAME + "\">" + "<gml:pos>" +
                toPointString() + "</gml:pos></gml:Point>";
    }

    public int getLatitude() {
        return latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GISPoint gisPoint = (GISPoint) o;

        return latitude == gisPoint.latitude && longitude == gisPoint.longitude;
    }

    @Override
    public int hashCode() {
        int result = latitude;
        result = 31 * result + longitude;
        return result;
    }

    @Override
    public String toString() {
        return "GISPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
