package fi.riista.feature.gamediary;

import fi.riista.feature.common.dto.CodesetEntryDTO;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;

import java.util.List;

public class GameDiaryParametersDTO {

    private final GameAge[] ages = GameAge.values();
    private final GameGender[] genders = GameGender.values();
    private final GameFitnessClass[] fitnessClasses = GameFitnessClass.values();
    private final GameAntlersType[] antlersTypes = GameAntlersType.values();

    private final List<CodesetEntryDTO> categories;
    private final List<GameSpeciesDTO> species;

    public GameDiaryParametersDTO(List<CodesetEntryDTO> categories, List<GameSpeciesDTO> species) {
        this.categories = categories;
        this.species = species;
    }

    public List<CodesetEntryDTO> getCategories() {
        return categories;
    }

    public List<GameSpeciesDTO> getSpecies() {
        return species;
    }

    public GameAge[] getAges() {
        return ages;
    }

    public GameGender[] getGenders() {
        return genders;
    }

    public GameFitnessClass[] getFitnessClasses() {
        return fitnessClasses;
    }

    public GameAntlersType[] getAntlersTypes() {
        return antlersTypes;
    }
}
