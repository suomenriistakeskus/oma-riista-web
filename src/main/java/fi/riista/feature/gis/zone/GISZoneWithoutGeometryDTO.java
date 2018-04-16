package fi.riista.feature.gis.zone;

import fi.riista.feature.common.entity.HasID;

import java.io.Serializable;
import java.util.Date;

public class GISZoneWithoutGeometryDTO implements HasID<Long>, Serializable {
    private final long id;
    private final GISZone.SourceType sourceType;
    private final double computedAreaSize;
    private final double waterAreaSize;
    private final Date modificationTime;

    public GISZoneWithoutGeometryDTO(final long id,
                                     final String sourceType,
                                     final double computedAreaSize,
                                     final double waterAreaSize,
                                     final Date modificationTime) {
        this.id = id;
        this.sourceType = GISZone.SourceType.valueOf(sourceType);
        this.computedAreaSize = computedAreaSize;
        this.waterAreaSize = waterAreaSize;
        this.modificationTime = modificationTime;
    }

    @Override
    public Long getId() {
        return id;
    }

    public GISZone.SourceType getSourceType() {
        return sourceType;
    }

    public double getComputedAreaSize() {
        return computedAreaSize;
    }

    public double getWaterAreaSize() {
        return waterAreaSize;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

}
