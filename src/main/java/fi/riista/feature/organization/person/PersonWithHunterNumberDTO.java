package fi.riista.feature.organization.person;

import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

public class PersonWithHunterNumberDTO extends PersonWithNameDTO {

    public static @Nonnull PersonWithHunterNumberDTO create(@Nonnull final Person person) {
        return new PersonWithHunterNumberDTO(person);
    }

    @FinnishHunterNumber
    private String hunterNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String extendedName;

    public PersonWithHunterNumberDTO() {
    }

    public PersonWithHunterNumberDTO(@Nonnull final PersonWithHunterNumberDTO other) {
        super(other);
        setHunterNumber(other.getHunterNumber());
        setExtendedName(other.getExtendedName());
    }

    public PersonWithHunterNumberDTO(@Nonnull final Person person) {
        super(person);
        setHunterNumber(person.getHunterNumber());
    }

    // Accessors -->

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getExtendedName() {
        return extendedName;
    }

    public void setExtendedName(final String extendedName) {
        this.extendedName = extendedName;
    }
}
