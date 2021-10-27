package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.gis.GISBounds;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MobileGroupHuntingAreaDTO {
    private final long areaId;
    private final String externalId;
    private final GISBounds bounds;

    public MobileGroupHuntingAreaDTO(final long areaId, final @Nonnull String externalId, final @Nonnull GISBounds bounds) {
        this.areaId = areaId;
        this.externalId = requireNonNull(externalId);
        this.bounds = requireNonNull(bounds);
    }

    public long getAreaId() {
        return areaId;
    }

    public String getExternalId() {
        return externalId;
    }

    public GISBounds getBounds() {
        return bounds;
    }
}
