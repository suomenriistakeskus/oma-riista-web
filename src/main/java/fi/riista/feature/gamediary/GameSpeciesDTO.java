package fi.riista.feature.gamediary;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fi.riista.feature.common.dto.CodesetEntryDTO;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonPropertyOrder({ "id", "code", "categoryId", "name", "multipleSpecimenAllowedOnHarvest" })
public class GameSpeciesDTO extends CodesetEntryDTO {

    @Nonnull
    public static List<GameSpeciesDTO> transformList(@Nonnull final Iterable<GameSpecies> speciesList) {
        Objects.requireNonNull(speciesList, "speciesList must not be null");

        return F.mapNonNullsToList(speciesList, GameSpeciesDTO::create);
    }

    @Nullable
    public static GameSpeciesDTO create(@Nullable final GameSpecies species) {
        return species == null
                ? null
                : new GameSpeciesDTO(
                        species.getOfficialCode(),
                        species.getCategory(),
                        species.getNameLocalisation(),
                        species.isMultipleSpecimenAllowedOnHarvest());
    }

    private final int categoryId;

    private final boolean multipleSpecimenAllowedOnHarvests;

    public GameSpeciesDTO(
            final int speciesCode,
            final GameCategory category,
            final String nameFi,
            final String nameSv,
            final boolean multipleSpecimenAllowedOnHarvests) {

        this(speciesCode, category, LocalisedString.of(nameFi, nameSv), multipleSpecimenAllowedOnHarvests);
    }

    public GameSpeciesDTO(
            final int speciesCode,
            final GameCategory category,
            final LocalisedString nameLocalisations,
            final boolean multipleSpecimenAllowedOnHarvests) {

        this(speciesCode, category, nameLocalisations.asMap(), multipleSpecimenAllowedOnHarvests);
    }

    private GameSpeciesDTO(
            final int speciesCode,
            final GameCategory category,
            final Map<String, String> nameLocalisations,
            final boolean multipleSpecimenAllowedOnHarvests) {

        super(speciesCode, nameLocalisations);

        this.categoryId = category.getOfficialCode();
        this.multipleSpecimenAllowedOnHarvests = multipleSpecimenAllowedOnHarvests;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public boolean isMultipleSpecimenAllowedOnHarvests() {
        return multipleSpecimenAllowedOnHarvests;
    }

}
