package fi.riista.feature.organization.person;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

public class PersonWithNameDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static PersonWithNameDTO create(@Nonnull final Person person) {
        return new PersonWithNameDTO(person);
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String byName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    public PersonWithNameDTO() {

    }

    public PersonWithNameDTO(@Nonnull final PersonWithNameDTO other) {
        super(other);
        setByName(other.getByName());
        setLastName(other.getLastName());
    }

    public PersonWithNameDTO(@Nonnull final Person person) {
        DtoUtil.copyBaseFields(person, this);
        setByName(person.getByName());
        setLastName(person.getLastName());
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(final String byName) {
        this.byName = byName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
}
