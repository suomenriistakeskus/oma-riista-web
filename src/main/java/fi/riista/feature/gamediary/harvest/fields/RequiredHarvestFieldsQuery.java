package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.common.entity.GeoLocation;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RequiredHarvestFieldsQuery {
    private final int gameSpeciesCode;
    private final LocalDate harvestDate;
    private final GeoLocation location;
    private final boolean withPermit;

    public RequiredHarvestFieldsQuery(final int gameSpeciesCode, final LocalDate harvestDate,
                                      final GeoLocation location, final boolean withPermit) {
        this.gameSpeciesCode = gameSpeciesCode;
        this.harvestDate = Objects.requireNonNull(harvestDate);
        this.location = Objects.requireNonNull(location);
        this.withPermit = withPermit;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    @Nonnull
    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    @Nonnull
    public GeoLocation getLocation() {
        return location;
    }

    public boolean isWithPermit() {
        return withPermit;
    }
}
