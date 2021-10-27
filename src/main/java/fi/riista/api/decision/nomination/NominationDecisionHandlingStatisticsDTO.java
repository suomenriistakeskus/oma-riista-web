package fi.riista.api.decision.nomination;

import fi.riista.feature.organization.OrganisationNameDTO;

public class NominationDecisionHandlingStatisticsDTO {

    private final OrganisationNameDTO rka;
    private final int ongoing;
    private final int published;

    public NominationDecisionHandlingStatisticsDTO(final OrganisationNameDTO rka, final int ongoing, final int published) {
        this.rka = rka;
        this.ongoing = ongoing;
        this.published = published;
    }
}
