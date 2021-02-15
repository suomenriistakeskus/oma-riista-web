package fi.riista.feature.common.decision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

public class ModeratorDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    public ModeratorDTO() {

    }

    public ModeratorDTO(@Nonnull final ModeratorDTO other) {
        super(other);
        setFirstName(other.getFirstName());
        setLastName(other.getLastName());
    }

    public ModeratorDTO(@Nonnull final SystemUser user) {
        DtoUtil.copyBaseFields(user, this);
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
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
