package fi.riista.feature.permit.application.carnivore.justification;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotBlank;

import static java.util.Objects.requireNonNull;

public class CarnivorePermitApplicationJustificationDTO {

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String populationAmount;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alternativeMeasures;

    public CarnivorePermitApplicationJustificationDTO() {
    }

    public CarnivorePermitApplicationJustificationDTO(final CarnivorePermitApplication carnivoreApplication,
                                                      final HarvestPermitApplicationSpeciesAmount speciesAmount) {

        requireNonNull(carnivoreApplication, "carnivoreApplication is null");
        requireNonNull(speciesAmount, "speciesAmount is null");

        this.populationAmount = speciesAmount.getPopulationAmount();
        this.alternativeMeasures = carnivoreApplication.getAlternativeMeasures();
    }

    public String getPopulationAmount() {
        return populationAmount;
    }

    public void setPopulationAmount(final String populationAmount) {
        this.populationAmount = populationAmount;
    }

    public String getAlternativeMeasures() {
        return alternativeMeasures;
    }

    public void setAlternativeMeasures(final String alternativeMeasures) {
        this.alternativeMeasures = alternativeMeasures;
    }
}
