package fi.riista.feature.shootingtest.official;

import fi.riista.feature.organization.person.Person;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ShootingTestOfficialDTO {

    private long id;
    private long shootingTestEventId;
    private long occupationId;
    private long personId;

    private String firstName;
    private String lastName;

    private Boolean shootingTestResponsible;

    public static ShootingTestOfficialDTO create(@Nonnull final ShootingTestOfficial official,
                                                 @Nonnull final Person person) {

        final ShootingTestOfficialDTO dto = new ShootingTestOfficialDTO();

        dto.setId(official.getId());
        dto.setShootingTestEventId(official.getShootingTestEvent().getId());
        dto.setOccupationId(official.getOccupation().getId());
        dto.setPersonId(person.getId());

        dto.setFirstName(person.getByName());
        dto.setLastName(person.getLastName());

        dto.setShootingTestResponsible(official.getShootingTestResponsible());

        return dto;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ShootingTestOfficialDTO)) {
            return false;
        } else {
            final ShootingTestOfficialDTO that = (ShootingTestOfficialDTO) o;

            return this.id == that.id
                    && this.shootingTestEventId == that.shootingTestEventId
                    && this.occupationId == that.occupationId
                    && this.personId == that.personId
                    && Objects.equals(this.firstName, that.firstName)
                    && Objects.equals(this.lastName, that.lastName)
                    && Objects.equals(this.shootingTestResponsible, that.shootingTestResponsible);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shootingTestEventId, occupationId, personId, firstName, lastName, shootingTestResponsible);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // Accessors -->

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

    public Boolean getShootingTestResponsible() {
        return shootingTestResponsible;
    }

    public void setShootingTestResponsible(final Boolean shootingTestResponsible) {
        this.shootingTestResponsible = shootingTestResponsible;
    }
}
