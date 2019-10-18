package fi.riista.feature.pub.occupation;

import java.util.List;

import static java.util.Collections.emptyList;

public class PublicOccupationsAndOrganisationsDTO {

    public static final PublicOccupationsAndOrganisationsDTO EMPTY_RESULT =
            new PublicOccupationsAndOrganisationsDTO(false, true, emptyList(), emptyList());

    public static final PublicOccupationsAndOrganisationsDTO TOO_MANY_RESULTS =
            new PublicOccupationsAndOrganisationsDTO(true, true, emptyList(), emptyList());

    private final boolean tooManyResults;

    private final boolean lastPage;

    private final List<PublicOccupationDTO> occupations;
    private final List<PublicOrganisationDTO> organisations;

    public PublicOccupationsAndOrganisationsDTO(final boolean lastPage,
                                                final List<PublicOccupationDTO> occupations,
                                                final List<PublicOrganisationDTO> organisations) {

        this(false, lastPage, occupations, organisations);
    }

    private PublicOccupationsAndOrganisationsDTO(final boolean tooManyResults,
                                                 final boolean lastPage,
                                                 final List<PublicOccupationDTO> occupations,
                                                 final List<PublicOrganisationDTO> organisations) {

        this.tooManyResults = tooManyResults;
        this.lastPage = lastPage;
        this.occupations = occupations;
        this.organisations = organisations;
    }

    public boolean isTooManyResults() {
        return tooManyResults;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public List<PublicOccupationDTO> getOccupations() {
        return occupations;
    }

    public List<PublicOrganisationDTO> getOrganisations() {
        return organisations;
    }

}
