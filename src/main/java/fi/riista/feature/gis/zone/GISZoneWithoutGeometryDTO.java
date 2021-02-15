package fi.riista.feature.gis.zone;

import fi.riista.feature.common.entity.HasID;
import org.joda.time.DateTime;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class GISZoneWithoutGeometryDTO implements HasID<Long>, Serializable {
    private final long id;
    private final GISZone.SourceType sourceType;
    private final GISZoneSizeDTO size;
    private final DateTime modificationTime;

    public GISZoneWithoutGeometryDTO(final Long id,
                                     final GISZoneSizeDTO size,
                                     final GISZone.SourceType sourceType,
                                     final DateTime modificationTime) {
        this.id = requireNonNull(id);
        this.sourceType = requireNonNull(sourceType);
        this.modificationTime = requireNonNull(modificationTime);
        this.size = requireNonNull(size);
    }

    @Override
    public Long getId() {
        return id;
    }

    public GISZoneSizeDTO getSize() {
        return size;
    }

    public GISZone.SourceType getSourceType() {
        return sourceType;
    }

    public DateTime getModificationTime() {
        return modificationTime;
    }
}
