package fi.riista.feature.permit.application.carnivore.justification;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public class CarnivorePermitApplicationJustificationDTO {

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String populationAmount;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String populationDescription;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalJustificationInfo;

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
        this.populationDescription = speciesAmount.getPopulationDescription();
        this.additionalJustificationInfo = carnivoreApplication.getAdditionalJustificationInfo();
        this.alternativeMeasures = carnivoreApplication.getAlternativeMeasures();
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

    public String getAdditionalJustificationInfo() {
        return additionalJustificationInfo;
    }

    public void setAdditionalJustificationInfo(final String additionalJustificationInfo) {
        this.additionalJustificationInfo = additionalJustificationInfo;
    }

    public String getAlternativeMeasures() {
        return alternativeMeasures;
    }

    public void setAlternativeMeasures(final String alternativeMeasures) {
        this.alternativeMeasures = alternativeMeasures;
    }
}
