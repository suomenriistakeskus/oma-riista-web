package fi.riista.feature.organization.rka;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class AreaMeetingRepresentativeDTO {

    private final OrganisationNameDTO organisation;
    private final PersonWithHunterNumberDTO person;
    private final PersonWithHunterNumberDTO substitute;

    static AreaMeetingRepresentativeDTO create(@Nonnull final OrganisationNameDTO organisation,
                                               @Nonnull final PersonWithHunterNumberDTO person,
                                               @Nonnull final PersonWithHunterNumberDTO substitute) {
        requireNonNull(organisation);
        requireNonNull(person);
        requireNonNull(substitute);
        return new AreaMeetingRepresentativeDTO(organisation, person, substitute);

    }

    public AreaMeetingRepresentativeDTO(final OrganisationNameDTO organisation,
                                        final PersonWithHunterNumberDTO person,
                                        final PersonWithHunterNumberDTO substitute) {
        this.organisation = organisation;
        this.person = person;
        this.substitute = substitute;
    }

    public OrganisationNameDTO getOrganisation() {
        return organisation;
    }

    public PersonWithHunterNumberDTO getPerson() {
        return person;
    }

    public PersonWithHunterNumberDTO getSubstitute() {
        return substitute;
    }
}
