package fi.riista.feature.shootingtest;

public class ShootingTestOfficialDTO {

    private long id;
    private long shootingTestEventId;
    private long occupationId;
    private long personId;

    private String firstName;
    private String lastName;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getShootingTestEventId() {
        return shootingTestEventId;
    }

    public void setShootingTestEventId(final long shootingTestEventId) {
        this.shootingTestEventId = shootingTestEventId;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
}
