package fi.riista.feature.pub.occupation;

import java.util.List;

import static java.util.Collections.emptyList;

public class PublicOccupationsAndOrganisationsDTO {

    public static final PublicOccupationsAndOrganisationsDTO EMPTY_RESULT =
            new PublicOccupationsAndOrganisationsDTO(emptyList(), emptyList());

    public static final PublicOccupationsAndOrganisationsDTO TOO_MANY_RESULTS =
            new PublicOccupationsAndOrganisationsDTO(true, emptyList(), emptyList());

    private final boolean tooManyResults;
    private final List<PublicOccupationDTO> occupations;
    private final List<PublicOrganisationDTO> organisations;

    public PublicOccupationsAndOrganisationsDTO(final List<PublicOccupationDTO> occupations,
                                                final List<PublicOrganisationDTO> organisations) {

        this(false, occupations, organisations);
    }

    private PublicOccupationsAndOrganisationsDTO(final boolean tooManyResults,
                                                 final List<PublicOccupationDTO> occupations,
                                                 final List<PublicOrganisationDTO> organisations) {

        this.tooManyResults = tooManyResults;
        this.occupations = occupations;
        this.organisations = organisations;
    }

    public boolean isTooManyResults() {
        return tooManyResults;
    }

    public List<PublicOccupationDTO> getOccupations() {
        return occupations;
    }

    public List<PublicOrganisationDTO> getOrganisations() {
        return organisations;
    }

}
