package fi.riista.feature.permit.application.nestremoval.period;

import javax.validation.Valid;
import java.util.List;

public class NestRemovalPermitApplicationSpeciesPeriodInformationDTO {

    public NestRemovalPermitApplicationSpeciesPeriodInformationDTO() {}

    public NestRemovalPermitApplicationSpeciesPeriodInformationDTO(
            final List<NestRemovalPermitApplicationSpeciesPeriodDTO> speciesPeriods) {
        this.speciesPeriods = speciesPeriods;
    }

    @Valid
    private List<NestRemovalPermitApplicationSpeciesPeriodDTO> speciesPeriods;

    public List<NestRemovalPermitApplicationSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public void setSpeciesPeriods(final List<NestRemovalPermitApplicationSpeciesPeriodDTO> speciesPeriods) {
        this.speciesPeriods = speciesPeriods;
    }
}
