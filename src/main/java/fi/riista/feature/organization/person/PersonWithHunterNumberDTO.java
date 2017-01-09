package fi.riista.feature.organization.person;

import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PersonWithHunterNumberDTO extends PersonWithNameDTO {

    public static @Nonnull PersonWithHunterNumberDTO create(@Nonnull final Person person) {
        final PersonWithHunterNumberDTO dto = new PersonWithHunterNumberDTO();
        dto.copyFieldsFrom(person);
        return dto;
    }

    public static @Nonnull PersonWithHunterNumberDTO create(
            @Nonnull final fi.riista.feature.organization.person.PersonDTO personDTO) {

        final PersonWithHunterNumberDTO dto = new PersonWithHunterNumberDTO();
        dto.copyFieldsFrom(personDTO);
        return dto;
    }

    public static @Nonnull List<PersonWithHunterNumberDTO> create(@Nonnull final List<Person> contactPersons) {
        return contactPersons.stream().map(PersonWithHunterNumberDTO::create).collect(toList());
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

    @Override
    protected void copyFieldsFrom(@Nonnull final Person person) {
        super.copyFieldsFrom(person);
        setHunterNumber(person.getHunterNumber());
    }

    @Override
    protected void copyFieldsFrom(@Nonnull final fi.riista.feature.organization.person.PersonDTO dto) {
        super.copyFieldsFrom(dto);
        setHunterNumber(dto.getHunterNumber());
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
