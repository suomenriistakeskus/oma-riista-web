package fi.riista.feature.gamediary;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameSpeciesDTO {

    @Nonnull
    public static List<GameSpeciesDTO> transformList(@Nonnull final Iterable<GameSpecies> speciesList) {
        Objects.requireNonNull(speciesList, "speciesList must not be null");

        return F.mapNonNullsToList(speciesList, GameSpeciesDTO::create);
    }

    @Nullable
    public static GameSpeciesDTO create(@Nullable final GameSpecies species) {
        return species == null ? null : new GameSpeciesDTO(
                species.getOfficialCode(),
                species.getCategory(),
                species.getNameLocalisation().asMap(),
                species.isMultipleSpecimenAllowedOnHarvest());
    }

    private final int code;
    private final Map<String, String> name;
    private final int categoryId;
    private final boolean multipleSpecimenAllowedOnHarvests;

    private GameSpeciesDTO(
            final int speciesCode,
            final GameCategory category,
            final Map<String, String> nameLocalisations,
            final boolean multipleSpecimenAllowedOnHarvests) {

        this.code = speciesCode;
        this.name = nameLocalisations;

        this.categoryId = category.getOfficialCode();
        this.multipleSpecimenAllowedOnHarvests = multipleSpecimenAllowedOnHarvests;
    }

    public int getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public boolean isMultipleSpecimenAllowedOnHarvests() {
        return multipleSpecimenAllowedOnHarvests;
    }
}
