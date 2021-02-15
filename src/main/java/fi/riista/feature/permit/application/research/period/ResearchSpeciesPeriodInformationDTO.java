package fi.riista.feature.permit.application.research.period;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class ResearchSpeciesPeriodInformationDTO {

    public ResearchSpeciesPeriodInformationDTO() {}

    public ResearchSpeciesPeriodInformationDTO(final List<ResearchSpeciesPeriodDTO> speciesPeriods,
                                               final Integer validityYears) {
        this.speciesPeriods = speciesPeriods;
        this.validityYears = validityYears;
    }

    @Valid
    private List<ResearchSpeciesPeriodDTO> speciesPeriods;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer validityYears;

    public List<ResearchSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final Integer validityYears) {
        this.validityYears = validityYears;
    }
}