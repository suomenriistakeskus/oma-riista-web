package fi.riista.feature.huntingclub.poi.mobile;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.huntingclub.poi.PoiLocation;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

public class MobilePoiLocationDTO implements HasID<Long> {

    private Long id;

    private Long poiId;

    private String description;

    @NotNull
    private Integer visibleId;

    @NotNull
    private GeoLocation geoLocation;

    public MobilePoiLocationDTO() {
    }

    public MobilePoiLocationDTO(@Nonnull final PoiLocation entity) {
        this.id = entity.getId();
        this.poiId = entity.getPoi().getId();
        this.description = entity.getDescription();
        this.visibleId = entity.getVisibleId();
        this.geoLocation = entity.getGeoLocation();
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Integer getVisibleId() {
        return visibleId;
    }

    public void setVisibleId(final Integer visibleId) {
        this.visibleId = visibleId;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
