package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport_;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class GroupHuntingDay extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hunting_group_id", nullable = false)
    private HuntingClubGroup group;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @Max(2 * 24 * 60 /* 2 days */)
    @Min(0)
    @Column(name = "break_duration_minutes")
    private Integer breakDurationInMinutes;

    // Snow depth in centimeters
    @Max(Short.MAX_VALUE)
    @Min(0)
    @Column
    private Integer snowDepth;

    @Column
    private Integer huntingMethod;

    @Max(Short.MAX_VALUE)
    @Min(1)
    @Column
    private Integer numberOfHunters;

    @Max(Short.MAX_VALUE)
    @Min(0)
    @Column
    private Integer numberOfHounds;

    @OneToMany(mappedBy = "huntingDayOfGroup")
    private Set<Harvest> harvests = new HashSet<>();

    @OneToMany(mappedBy = "huntingDayOfGroup", fetch = FetchType.LAZY)
    private Set<Observation> observations = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private MooseDataCardImport mooseDataCardImport;

    public GroupHuntingDay() {
    }

    public GroupHuntingDay(final HuntingClubGroup group, final DateTime start, final DateTime end) {
        setGroup(group);

        if (start != null) {
            this.startTime = start.toLocalTime();
            this.startDate = start.toLocalDate();
        }
        if (end != null) {
            this.endDate = end.toLocalDate();
            this.endTime = end.toLocalTime();
        }
    }

    public LocalDateTime getStartAsLocalDateTime() {
        return getStartDate().toLocalDateTime(getStartTime());
    }

    public LocalDateTime getEndAsLocalDateTime() {
        return getEndDate().toLocalDateTime(getEndTime());
    }

    public int calculateHuntingDayDurationInMinutes() {
        final Duration duration = DateUtil.toDuration(startDate, startTime, endDate, endTime);
        return duration.toStandardMinutes()
                .minus(F.firstNonNull(breakDurationInMinutes, 0))
                .getMinutes();
    }

    public boolean containsInstant(@Nonnull final DateTime instant) {
        return containsInstant(instant.toLocalDateTime());
    }

    public boolean containsInstant(@Nonnull final LocalDateTime instant) {
        Objects.requireNonNull(instant);

        final LocalDateTime start = getStartAsLocalDateTime();
        final LocalDateTime end = getEndAsLocalDateTime();

        return (instant.isEqual(start) || instant.isAfter(start)) && (instant.isEqual(end) || instant.isBefore(end));
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_hunting_day_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingClubGroup getGroup() {
        return group;
    }

    public void setGroup(final HuntingClubGroup group) {
        CriteriaUtils.updateInverseCollection(HuntingClubGroup_.huntingDays, this, this.group, group);
        this.group = group;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
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

    public Integer getBreakDurationInMinutes() {
        return breakDurationInMinutes;
    }

    public void setBreakDurationInMinutes(final Integer breakDurationInMinutes) {
        this.breakDurationInMinutes = breakDurationInMinutes;
    }

    public Integer getSnowDepth() {
        return snowDepth;
    }

    public void setSnowDepth(final Integer snowDepth) {
        this.snowDepth = snowDepth;
    }

    public GroupHuntingMethod getHuntingMethod() {
        return GroupHuntingMethod.valueOf(this.huntingMethod);
    }

    public void setHuntingMethod(final GroupHuntingMethod value) {
        this.huntingMethod = value == null ? null : value.getTypeCode();
    }

    public Integer getNumberOfHunters() {
        return numberOfHunters;
    }

    public void setNumberOfHunters(final Integer numberOfHunters) {
        this.numberOfHunters = numberOfHunters;
    }

    public Integer getNumberOfHounds() {
        return numberOfHounds;
    }

    public void setNumberOfHounds(final Integer numberOfHounds) {
        this.numberOfHounds = numberOfHounds;
    }

    public MooseDataCardImport getMooseDataCardImport() {
        return mooseDataCardImport;
    }

    public void setMooseDataCardImport(final MooseDataCardImport mooseDataCardImport) {
        CriteriaUtils.updateInverseCollection(
                MooseDataCardImport_.huntingDays, this, this.mooseDataCardImport, mooseDataCardImport);
        this.mooseDataCardImport = mooseDataCardImport;
    }

    Set<Harvest> getHarvests() {
        return harvests;
    }

    Set<Observation> getObservations() {
        return observations;
    }
}
