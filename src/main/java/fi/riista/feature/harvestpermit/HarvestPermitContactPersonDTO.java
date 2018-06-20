package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DtoUtil;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestPermitContactPersonDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static HarvestPermitContactPersonDTO create(@Nonnull final Person person) {
        Objects.requireNonNull(person, "person must not be null");

        HarvestPermitContactPersonDTO dto = new HarvestPermitContactPersonDTO();
        DtoUtil.copyBaseFields(person, dto);

        dto.setHunterNumber(person.getHunterNumber());
        dto.setByName(person.getByName());
        dto.setLastName(person.getLastName());

        return dto;
    }

    private Long id;
    private Integer rev;
    private boolean canBeDeleted;

    @FinnishHunterNumber
    private String hunterNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String byName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(String byName) {
        this.byName = byName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
    }
}
