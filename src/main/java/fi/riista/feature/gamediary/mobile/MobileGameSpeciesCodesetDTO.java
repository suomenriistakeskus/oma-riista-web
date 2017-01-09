package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.dto.CodesetEntryDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;

import java.util.List;

public class MobileGameSpeciesCodesetDTO {

    private final List<CodesetEntryDTO> categories;

    private final List<GameSpeciesDTO> species;

    public MobileGameSpeciesCodesetDTO(List<CodesetEntryDTO> categories, List<GameSpeciesDTO> species) {
        this.categories = categories;
        this.species = species;
    }

    public List<CodesetEntryDTO> getCategories() {
        return categories;
    }

    public List<GameSpeciesDTO> getSpecies() {
        return species;
    }

}
