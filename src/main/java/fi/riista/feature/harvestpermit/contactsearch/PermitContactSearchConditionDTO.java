package fi.riista.feature.harvestpermit.contactsearch;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;

public class PermitContactSearchConditionDTO {

    private HarvestPermitCategory harvestPermitCategory;
    private Integer huntingYear;

    public PermitContactSearchConditionDTO() {
    }

    public PermitContactSearchConditionDTO(final HarvestPermitCategory harvestPermitCategory, final Integer huntingYear) {
        this.harvestPermitCategory = harvestPermitCategory;
        this.huntingYear = huntingYear;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }
}
