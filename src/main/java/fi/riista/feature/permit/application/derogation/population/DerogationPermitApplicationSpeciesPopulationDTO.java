package fi.riista.feature.permit.application.derogation.population;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public class DerogationPermitApplicationSpeciesPopulationDTO {
    private int gameSpeciesCode;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String populationAmount;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String populationDescription;

    public DerogationPermitApplicationSpeciesPopulationDTO() {
    }

    public DerogationPermitApplicationSpeciesPopulationDTO(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.populationAmount = speciesAmount.getPopulationAmount();
        this.populationDescription = speciesAmount.getPopulationDescription();
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public String getPopulationAmount() {
        return populationAmount;
    }

    public void setPopulationAmount(final String populationAmount) {
        this.populationAmount = populationAmount;
    }

    public String getPopulationDescription() {
        return populationDescription;
    }

    public void setPopulationDescription(final String populationDescription) {
        this.populationDescription = populationDescription;
    }
}
