package fi.riista.feature.harvestpermit.search;

import com.google.common.base.Preconditions;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.Set;

public class HarvestPermitTypeDTO {

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitTypeCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    private Set<Integer> speciesCodes;

    public HarvestPermitTypeDTO() {
    }

    public HarvestPermitTypeDTO(final String permitTypeCode,
                                final String permitType,
                                final Set<Integer> speciesCodes) {
        Preconditions.checkArgument(permitTypeCode.length() == 3, "Invalid permitTypeCode:" + permitTypeCode);
        this.permitTypeCode = permitTypeCode;
        this.permitType = permitType;
        this.speciesCodes = speciesCodes;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public Set<Integer> getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(Set<Integer> speciesCodes) {
        this.speciesCodes = speciesCodes;
    }
}
