package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.gis.GISBounds;

import java.util.Objects;

public class GroupHuntingAreaDTO {
    private Long areaId;
    private GISBounds bounds;

    public GroupHuntingAreaDTO(final Long areaId, final GISBounds bounds) {
        this.areaId = Objects.requireNonNull(areaId);
        this.bounds = Objects.requireNonNull(bounds);
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(final Long areaId) {
        this.areaId = areaId;
    }

    public GISBounds getBounds() {
        return bounds;
    }

    public void setBounds(final GISBounds bounds) {
        this.bounds = bounds;
    }
}
