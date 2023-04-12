package fi.riista.feature.huntingclub.poi.gpx;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.huntingclub.poi.PointOfInterestType;

public class GpxPoiLocationDTO implements HasID<Long> {

    private Long id;
    private long poiId;
    private String poiDescription;
    private String locationComment;
    private String visibleId;
    private PointOfInterestType type;
    private double latitude;
    private double longitude;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public long getPoiId() {
        return poiId;
    }

    public void setPoiId(final long poiId) {
        this.poiId = poiId;
    }

    public String getPoiDescription() {
        return poiDescription;
    }

    public void setPoiDescription(final String poiDescription) {
        this.poiDescription = poiDescription;
    }

    public String getLocationComment() {
        return locationComment;
    }

    public void setLocationComment(final String locationComment) {
        this.locationComment = locationComment;
    }

    public String getVisibleId() {
        return visibleId;
    }

    public void setVisibleId(final String visibleId) {
        this.visibleId = visibleId;
    }

    public PointOfInterestType getType() {
        return type;
    }

    public void setType(final PointOfInterestType type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
}
