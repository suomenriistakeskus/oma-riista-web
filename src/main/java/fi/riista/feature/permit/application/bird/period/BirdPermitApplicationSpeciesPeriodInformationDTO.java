package fi.riista.feature.permit.application.bird.period;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BirdPermitApplicationSpeciesPeriodInformationDTO {

    public BirdPermitApplicationSpeciesPeriodInformationDTO() {

    }

    public BirdPermitApplicationSpeciesPeriodInformationDTO(List<BirdPermitApplicationSpeciesPeriodDTO> speciesPeriods,
                                                            Integer validityYears,
                                                            boolean limitlessPermitAllowed) {
        this.speciesPeriods = speciesPeriods;
        this.validityYears = validityYears;
        this.limitlessPermitAllowed = limitlessPermitAllowed;
    }

    @Valid
    private List<BirdPermitApplicationSpeciesPeriodDTO> speciesPeriods;

    @NotNull
    @Min(0)
    @Max(5)
    private Integer validityYears;

    private boolean limitlessPermitAllowed;

    public List<BirdPermitApplicationSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final Integer validityYears) {
        this.validityYears = validityYears;
    }

    public boolean isLimitlessPermitAllowed() {
        return limitlessPermitAllowed;
    }

    public void setLimitlessPermitAllowed(boolean limitlessPermitAllowed) {
        this.limitlessPermitAllowed = limitlessPermitAllowed;
    }
}
