package fi.riista.feature.gis.kiinteisto;

import fi.riista.feature.common.entity.PropertyIdentifier;

import static java.util.Objects.requireNonNull;

public class GISPropertyExcelRow {
    private final Integer palstaId;
    private final String propertyName;
    private final Double originalSize;
    private final Double excludedSize;
    private final PropertyIdentifier propertyIdentifier;
    private final boolean changed;

    public GISPropertyExcelRow(final Integer palstaId,
                               final PropertyIdentifier propertyIdentifier,
                               final String propertyName,
                               final Double originalSize,
                               final Double excludedSize,
                               final Boolean changed) {

        this.palstaId = requireNonNull(palstaId);
        this.propertyIdentifier = requireNonNull(propertyIdentifier);
        this.propertyName = propertyName;
        this.originalSize = requireNonNull(originalSize);
        this.excludedSize = requireNonNull(excludedSize);
        this.changed = requireNonNull(changed);
    }

    public Double formatActualSize() {
        return excludedSize > 1.0
                ? (originalSize - excludedSize) / 10_000
                : originalSize / 10_000;
    }

    public Double formatOriginalSize() {
        return excludedSize > 1.0 ? originalSize / 10_000 : null;
    }

    public Integer getPalstaId() {
        return palstaId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public PropertyIdentifier getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public boolean isChanged() {
        return changed;
    }
}
