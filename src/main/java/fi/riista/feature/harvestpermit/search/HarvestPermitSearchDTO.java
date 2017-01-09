package fi.riista.feature.harvestpermit.search;

import org.hibernate.validator.constraints.SafeHtml;

public class HarvestPermitSearchDTO {

    // @FinnishHuntingPermitNumber
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;
    private Long areaId;
    private Integer speciesCode;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String year;

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }
    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Integer getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(Integer speciesCode) {
        this.speciesCode = speciesCode;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
