package fi.riista.feature.permit.application.statistics;

import fi.riista.feature.organization.OrganisationNameDTO;

import java.util.List;

public class HarvestPermitApplicationStatusTableDTO {
    private OrganisationNameDTO rka;
    private List<HarvestPermitApplicationStatusItemDTO> categoryStatuses;

    public OrganisationNameDTO getRka() {
        return rka;
    }

    public void setRka(final OrganisationNameDTO rka) {
        this.rka = rka;
    }

    public List<HarvestPermitApplicationStatusItemDTO> getCategoryStatuses() {
        return categoryStatuses;
    }

    public void setCategoryStatuses(final List<HarvestPermitApplicationStatusItemDTO> categoryStatuses) {
        this.categoryStatuses = categoryStatuses;
    }
}
