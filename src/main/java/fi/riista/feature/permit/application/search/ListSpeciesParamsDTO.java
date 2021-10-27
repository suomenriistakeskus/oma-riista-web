package fi.riista.feature.permit.application.search;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;

public class ListSpeciesParamsDTO {

    private HarvestPermitCategory permitCategory;

    public HarvestPermitCategory getPermitCategory() {
        return permitCategory;
    }

    public void setPermitCategory(final HarvestPermitCategory permitCategory) {
        this.permitCategory = permitCategory;
    }
}
