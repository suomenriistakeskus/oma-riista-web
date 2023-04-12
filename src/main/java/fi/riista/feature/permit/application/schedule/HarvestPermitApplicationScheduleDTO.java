package fi.riista.feature.permit.application.schedule;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

public class HarvestPermitApplicationScheduleDTO extends BaseEntityDTO<Long> {

    public static HarvestPermitApplicationScheduleDTO create(@Nonnull final HarvestPermitApplicationSchedule schedule) {
        Objects.requireNonNull(schedule);

        final HarvestPermitApplicationScheduleDTO dto = new HarvestPermitApplicationScheduleDTO();
        DtoUtil.copyBaseFields(schedule, dto);
        dto.setCategory(schedule.getCategory());
        dto.setBeginTime(DateUtil.toLocalDateTimeNullSafe(schedule.getBeginTime()));
        dto.setEndTime(DateUtil.toLocalDateTimeNullSafe(schedule.getEndTime()));
        dto.setInstructions(LocalisedString.of(schedule.getInstructionsFi(), schedule.getInstructionsSv()).asMap());
        dto.setActiveOverride(schedule.getActiveOverride());

        return dto;
    }

    private Long id;
    private Integer rev;

    @NotNull
    private HarvestPermitCategory category;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Map<String, String> instructions;
    private Boolean activeOverride;

    public HarvestPermitApplicationScheduleDTO() {
    }

    @AssertTrue
    public boolean isStateValid() {
        if (activeOverride != null) {
            return beginTime == null && endTime == null;
        }

        return beginTime != null && endTime != null;
    }

    @AssertTrue
    public boolean isTimeValid() {
        if (beginTime != null && endTime != null) {
            return !beginTime.isAfter(endTime);
        }

        return true;
    }

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


    public HarvestPermitCategory getCategory() {
        return category;
    }

    public void setCategory(final HarvestPermitCategory category) {
        this.category = category;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(final LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Map<String, String> getInstructions() {
        return instructions;
    }

    public void setInstructions(final Map<String, String> instructions) {
        this.instructions = instructions;
    }

    public Boolean getActiveOverride() {
        return activeOverride;
    }

    public void setActiveOverride(final Boolean activeOverride) {
        this.activeOverride = activeOverride;
    }
}
