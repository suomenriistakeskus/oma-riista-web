package fi.riista.feature.gis.zone;

import fi.riista.feature.common.entity.HasID;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class GISZoneWithoutGeometryDTO implements HasID<Long>, Serializable {
    private final long id;
    private final GISZone.SourceType sourceType;
    private final GISZoneSizeDTO size;
    private final Date modificationTime;

    public GISZoneWithoutGeometryDTO(final Long id,
                                     final GISZoneSizeDTO size,
                                     final GISZone.SourceType sourceType,
                                     final Date modificationTime) {
        this.id = Objects.requireNonNull(id);
        this.sourceType = Objects.requireNonNull(sourceType);
        this.modificationTime = Objects.requireNonNull(modificationTime);
        this.size = size;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Nullable
    public GISZoneSizeDTO getSize() {
        return size;
    }

    public GISZone.SourceType getSourceType() {
        return sourceType;
    }

    public Date getModificationTime() {
        return modificationTime;
    }
}
