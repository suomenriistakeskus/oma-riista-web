package fi.riista.feature.gis.zone;

import fi.riista.feature.common.entity.HasID;

public class GISZoneWithoutGeometryDTO implements HasID<Long> {
    private final long id;
    private final GISZone.SourceType sourceType;
    private final double computedAreaSize;
    private final double waterAreaSize;

    public GISZoneWithoutGeometryDTO(final long id,
                                     final String sourceType,
                                     final double computedAreaSize,
                                     final double waterAreaSize) {
        this.id = id;
        this.sourceType = GISZone.SourceType.valueOf(sourceType);
        this.computedAreaSize = computedAreaSize;
        this.waterAreaSize = waterAreaSize;
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
}
