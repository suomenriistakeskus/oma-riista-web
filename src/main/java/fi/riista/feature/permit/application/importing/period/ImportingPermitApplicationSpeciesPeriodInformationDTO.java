package fi.riista.feature.permit.application.importing.period;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;


public class ImportingPermitApplicationSpeciesPeriodInformationDTO {

    public ImportingPermitApplicationSpeciesPeriodInformationDTO() {

    }

    public ImportingPermitApplicationSpeciesPeriodInformationDTO(
            final List<ImportingPermitApplicationSpeciesPeriodDTO> speciesPeriods,
            final Integer validityYears) {
        this.speciesPeriods = speciesPeriods;
        this.validityYears = validityYears;
    }

    @Valid
    private List<ImportingPermitApplicationSpeciesPeriodDTO> speciesPeriods;

    // Boxed Integer used for draft outbound dtos, incoming dtos must have field not null
    @NotNull
    @Min(1)
    @Max(5)
    private Integer validityYears;

    public List<ImportingPermitApplicationSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public Integer getValidityYears() {
        return validityYears;
    }
}
