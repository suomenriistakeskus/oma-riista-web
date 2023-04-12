package fi.riista.feature.organization.rka;

import fi.riista.feature.organization.OrganisationNameDTO;

import java.util.List;

public class AreaMeetingRhyRepresentativesDTO {

    private final OrganisationNameDTO rhy;
    private final List<RepresentativePersonDTO> representatives;
    private final List<RepresentativePersonDTO> substituteRepresentatives;

    public AreaMeetingRhyRepresentativesDTO(final OrganisationNameDTO rhy,
                                            final List<RepresentativePersonDTO> representatives,
                                            final List<RepresentativePersonDTO> substituteRepresentatives) {
        this.rhy = rhy;
        this.representatives = representatives;
        this.substituteRepresentatives = substituteRepresentatives;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public List<RepresentativePersonDTO> getRepresentatives() {
        return representatives;
    }

    public List<RepresentativePersonDTO> getSubstituteRepresentatives() {
        return substituteRepresentatives;
    }
}
