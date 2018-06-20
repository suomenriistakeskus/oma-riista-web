package fi.riista.feature.gamediary;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionSupport;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class HuntingDiaryEntryDTO extends GameDiaryEntryDTO {

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    private int gameSpeciesCode;

    public HuntingDiaryEntryDTO(@Nonnull final GameDiaryEntryType type) {
        super(type);
    }

    // Accessors -->

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    protected static abstract class Builder<DTO extends HuntingDiaryEntryDTO, SELF extends Builder<DTO, SELF>>
            extends GameDiaryEntryDTO.Builder<DTO, SELF> {

        public SELF withGameSpeciesCode(final int gameSpeciesCode) {
            dto.setGameSpeciesCode(gameSpeciesCode);
            return self();
        }

        public SELF populateWith(@Nonnull final GameSpecies species) {
            Objects.requireNonNull(species);
            return withGameSpeciesCode(species.getOfficialCode());
        }
    }

}
