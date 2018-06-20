package fi.riista.feature.organization.occupation.search;

import org.hibernate.validator.constraints.SafeHtml;

public class RhyContactSearchDTO {

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String areaCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyCode;

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
}
