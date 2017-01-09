package fi.riista.feature.huntingclub.hunting.day;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GroupHuntingDayDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @NotNull
    private Long huntingGroupId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime startTime;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime endTime;

    // Computed field only for display
    private Integer durationInMinutes;

    @NotNull
    @Max(2 * 24 * 60 /* 2 days */)
    @Min(0)
    private Integer breakDurationInMinutes;

    @Min(0)
    private Integer snowDepth;

    private GroupHuntingMethod huntingMethod;

    private Integer numberOfHunters;

    private Integer numberOfHounds;

    // Builder methods

    public GroupHuntingDayDTO withNumberOfHounds(final Integer numberOfHounds) {
        setNumberOfHounds(numberOfHounds);
        return this;
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
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public Long getHuntingGroupId() {
        return huntingGroupId;
    }

    public void setHuntingGroupId(final Long huntingGroupId) {
        this.huntingGroupId = huntingGroupId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(final Integer durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public Integer getBreakDurationInMinutes() {
        return breakDurationInMinutes;
    }

    public void setBreakDurationInMinutes(final Integer breakDurationInMinutes) {
        this.breakDurationInMinutes = breakDurationInMinutes;
    }

    public Integer getSnowDepth() {
        return snowDepth;
    }

    public void setSnowDepth(Integer snowDepth) {
        this.snowDepth = snowDepth;
    }

    public GroupHuntingMethod getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(GroupHuntingMethod huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Integer getNumberOfHunters() {
        return numberOfHunters;
    }

    public Integer getNumberOfHounds() {
        return numberOfHounds;
    }

    public void setNumberOfHounds(final Integer numberOfHounds) {
        this.numberOfHounds = numberOfHounds;
    }

    public void setNumberOfHunters(Integer numberOfHunters) {
        this.numberOfHunters = numberOfHunters;
    }
}
