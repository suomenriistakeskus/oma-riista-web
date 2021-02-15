package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.common.entity.GeoLocation;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class RequiredHarvestFieldsRequestDTO {

    private final int gameSpeciesCode;
    private final LocalDate harvestDate;
    private final GeoLocation location;
    private final boolean withPermit;

    // TODO `personId` may be removed when deer pilot 2020 is over.
    private final Long personId;

    public RequiredHarvestFieldsRequestDTO(final int gameSpeciesCode,
                                           @Nonnull final LocalDate harvestDate,
                                           @Nonnull final GeoLocation location,
                                           final boolean withPermit,
                                           @Nullable final Long personId) {

        this.gameSpeciesCode = gameSpeciesCode;
        this.harvestDate = requireNonNull(harvestDate);
        this.location = requireNonNull(location);
        this.withPermit = withPermit;
        this.personId = personId;
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

    public Long getPersonId() {
        return personId;
    }
}
