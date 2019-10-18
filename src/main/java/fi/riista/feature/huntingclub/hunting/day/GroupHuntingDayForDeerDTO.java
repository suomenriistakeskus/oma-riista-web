package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.common.dto.BaseEntityDTO;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;

public class GroupHuntingDayForDeerDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @NotNull
    private Long huntingGroupId;

    @NotNull
    private LocalDate date;

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
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public Long getHuntingGroupId() {
        return huntingGroupId;
    }

    public void setHuntingGroupId(Long huntingGroupId) {
        this.huntingGroupId = huntingGroupId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
