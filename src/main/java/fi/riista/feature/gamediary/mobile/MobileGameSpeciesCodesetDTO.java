package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameCategoryDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;

import java.util.List;

public class MobileGameSpeciesCodesetDTO {

    private final List<GameCategoryDTO> categories;

    private final List<GameSpeciesDTO> species;

    public MobileGameSpeciesCodesetDTO(List<GameCategoryDTO> categories, List<GameSpeciesDTO> species) {
        this.categories = categories;
        this.species = species;
    }

    public List<GameCategoryDTO> getCategories() {
        return categories;
    }

    public List<GameSpeciesDTO> getSpecies() {
        return species;
    }

}
