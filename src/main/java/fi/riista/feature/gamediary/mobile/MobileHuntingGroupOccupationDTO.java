package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MobileHuntingGroupOccupationDTO extends BaseEntityDTO<Long> implements HasBeginAndEndDate {

    private long id;

    private final OccupationType occupationType;

    private final long personId;

    @ApiModelProperty(required = true)
    private final String firstName;
    @ApiModelProperty(required = true)
    private final String lastName;

    private final String hunterNumber;

    private final LocalDate beginDate;
    private final LocalDate endDate;

    public MobileHuntingGroupOccupationDTO(@Nonnull final Occupation occupation, @Nonnull final Person person) {
        super();
        requireNonNull(occupation);
        requireNonNull(person);

        this.id = occupation.getId();
        this.occupationType = occupation.getOccupationType();
        this.beginDate = occupation.getBeginDate();
        this.endDate = occupation.getEndDate();

        this.personId = person.getId();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.hunterNumber = person.getHunterNumber();
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return null;
    }

    @Override
    public void setRev(final Integer rev) {

    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public long getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }
}
