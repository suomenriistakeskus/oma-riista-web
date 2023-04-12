package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;

import javax.validation.Valid;
import java.util.List;

public class AnnualRenewalPeriodUpdateDTO {

    private long permitId;

    @Valid
    private List<SpeciesPeriodDTO> periods;

    public long getPermitId() {
        return permitId;
    }

    public void setPermitId(final long permitId) {
        this.permitId = permitId;
    }

    public List<SpeciesPeriodDTO> getPeriods() {
        return periods;
    }

    public void setPeriods(final List<SpeciesPeriodDTO> periods) {
        this.periods = periods;
    }

    public static class SpeciesPeriodDTO  extends Has2BeginEndDatesDTO {
        private int speciesCode;

        public SpeciesPeriodDTO() {}

        public SpeciesPeriodDTO(final int speciesCode) {
            this.speciesCode = speciesCode;
        }

        public int getSpeciesCode() {
            return speciesCode;
        }

        public void setSpeciesCode(final int speciesCode) {
            this.speciesCode = speciesCode;
        }
    }
}
