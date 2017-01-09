package fi.riista.feature.organization.person;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DtoUtil;

import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

import java.util.Objects;

public class PersonWithNameDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static PersonWithNameDTO create(@Nonnull final Person person) {
        final PersonWithNameDTO dto = new PersonWithNameDTO();
        dto.copyFieldsFrom(person);
        return dto;
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

    protected void copyFieldsFrom(@Nonnull final Person person) {
        Objects.requireNonNull(person);

        DtoUtil.copyBaseFields(person, this);
        setByName(person.getByName());
        setLastName(person.getLastName());
    }

    protected void copyFieldsFrom(@Nonnull final fi.riista.feature.organization.person.PersonDTO dto) {
        Objects.requireNonNull(dto);

        setId(dto.getId());
        setRev(dto.getRev());
        setByName(dto.getByName());
        setLastName(dto.getLastName());
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
