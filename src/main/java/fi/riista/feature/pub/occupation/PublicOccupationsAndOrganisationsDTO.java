package fi.riista.feature.pub.occupation;

import java.util.List;

public class PublicOccupationsAndOrganisationsDTO {

    private final List<PublicOccupationDTO> occupations;
    private final List<PublicOrganisationDTO> organisations;

    public PublicOccupationsAndOrganisationsDTO(
            List<PublicOccupationDTO> occupations, List<PublicOrganisationDTO> organisations) {

        this.occupations = occupations;
        this.organisations = organisations;
    }

    public List<PublicOccupationDTO> getOccupations() {
        return occupations;
    }

    public List<PublicOrganisationDTO> getOrganisations() {
        return organisations;
    }

}
