package fi.riista.feature.organization.rka;

import fi.riista.feature.organization.person.Person;

public class RepresentativePersonDTO {

    final Long organisationId;
    final String byName;
    final String lastName;

    public static RepresentativePersonDTO create(final long rhyId, final Person person) {
        return new RepresentativePersonDTO(rhyId, person.getByName(), person.getLastName());
    }

    private RepresentativePersonDTO(final Long organisationId, final String byName, final String lastName) {
        this.organisationId = organisationId;
        this.byName = byName;
        this.lastName = lastName;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public String getByName() {
        return byName;
    }

    public String getLastName() {
        return lastName;
    }
}
