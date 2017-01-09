package fi.riista.feature.organization.occupation.search;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.OrganisationType;
import org.hibernate.validator.constraints.NotEmpty;

public class OccupationContactSearchDTO {

    @NotEmpty // this is required, it's used if nothing else is specified
    private OrganisationType organisationType;
    private String areaCode;
    private String rhyCode;
    private OccupationType occupationType;

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(OrganisationType organisationType) {
        this.organisationType = organisationType;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    @Override
    public String toString() {
        return "ContactSearchDTO{" +
                "organisationType=" + organisationType +
                ", areaCode='" + areaCode + '\'' +
                ", rhyCode='" + rhyCode + '\'' +
                ", occupationType=" + occupationType +
                '}';
    }
}
