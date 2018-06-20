package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.Patterns;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RhyBasicInfoDTO {

    public static RhyBasicInfoDTO create(final RhyBasicInfo basicInfo) {
        final RhyBasicInfoDTO dto = new RhyBasicInfoDTO();
        dto.setIban(basicInfo.getIbanAsFormattedString());
        dto.setOperationalLandAreaSize(basicInfo.getOperationalLandAreaSize());
        dto.setRhyMembers(basicInfo.getRhyMembers());
        return dto;
    }

    // Max size allows spaces in-between for values transferred in DTO.
    @Size(max = 22)
    @Pattern(regexp = "^$|(" + Patterns.IBAN_FINNISH + ")")
    private String iban;

    // Toiminta-alueen maapinta-alue, ha
    @Min(0)
    private Integer operationalLandAreaSize;

    @Min(0)
    private Integer rhyMembers;

    // Accessors -->

    public String getIban() {
        return iban;
    }

    public void setIban(final String iban) {
        this.iban = iban;
    }

    public Integer getOperationalLandAreaSize() {
        return operationalLandAreaSize;
    }

    public void setOperationalLandAreaSize(final Integer operationalLandAreaSize) {
        this.operationalLandAreaSize = operationalLandAreaSize;
    }

    public Integer getRhyMembers() {
        return rhyMembers;
    }

    public void setRhyMembers(final Integer rhyMembers) {
        this.rhyMembers = rhyMembers;
    }
}
