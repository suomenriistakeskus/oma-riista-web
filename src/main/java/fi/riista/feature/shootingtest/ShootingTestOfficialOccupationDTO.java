package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;

public class ShootingTestOfficialOccupationDTO {

    private long occupationId;
    private long personId;
    private String lastName;
    private String firstName;

    public static ShootingTestOfficialOccupationDTO create(final Occupation occupation) {
        final Person person = occupation.getPerson();

        return new ShootingTestOfficialOccupationDTO(
                occupation.getId(), person.getId(), person.getLastName(), person.getFirstName());
    }

    public ShootingTestOfficialOccupationDTO(final long occupationId,
                                             final long personId,
                                             final String lastName,
                                             final String firstName) {
        this.occupationId = occupationId;
        this.personId = personId;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public long getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(final long occupationId) {
        this.occupationId = occupationId;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(final long personId) {
        this.personId = personId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
}
