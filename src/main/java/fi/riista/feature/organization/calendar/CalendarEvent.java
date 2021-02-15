package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.Organisation;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static fi.riista.util.DateUtil.today;

@Entity
@Access(value = AccessType.FIELD)
public class CalendarEvent extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Venue venue;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Organisation organisation;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CalendarEventType calendarEventType;

    @Size(max = 255)
    @Column
    private String name;

    @NotNull
    @Column(nullable = false)
    private Date date;

    @NotNull
    @Column(nullable = false)
    private LocalTime beginTime;

    @Column
    private LocalTime endTime;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private boolean publicVisibility;

    @Column(nullable = false)
    private boolean excludedFromStatistics;

    @Column
    @Min(0)
    private Integer participants;

    @OneToMany(mappedBy = "calendarEvent")
    private Set<AdditionalCalendarEvent> additionalCalendarEvents = new HashSet<>();

    @Column(nullable = false)
    private boolean remoteEvent;

    public CalendarEvent() {
    }

    public CalendarEvent(Organisation organisation, Venue venue, CalendarEventType calendarEventType, Date date, LocalTime beginTime, String name, String description) {
        this.name = name;
        this.date = date;
        this.beginTime = beginTime;
        this.organisation = organisation;
        this.calendarEventType = calendarEventType;
        this.venue = venue;
        this.description = description;
    }

    @Transient
    public boolean isLockedAsPastCalendarEvent() {
        if (calendarEventType == null || calendarEventType.isShootingTest()) {
            return false;
        }

        return today().isAfter(getDateAsLocalDate()) && participants != null && participants > 0;
    }

    @Transient
    public boolean isLockedAsPastStatistics() {
        if (calendarEventType == null) {
            return false;
        }
        final LocalDate today = today();
        final LocalDate eventDate = getDateAsLocalDate();

        return eventDate.getYear() < today.minusDays(15).getYear();
    }

    public LocalDate getDateAsLocalDate() {
        return DateUtil.toLocalDateNullSafe(date);
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "calendar_event_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(final Venue venue) {
        this.venue = venue;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(final Organisation organisation) {
        this.organisation = organisation;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(final CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(final LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean getPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(final boolean publicVisibility) {
        this.publicVisibility = publicVisibility;
    }

    public boolean getExcludedFromStatistics() {
        return excludedFromStatistics;
    }

    public void setExcludedFromStatistics(final boolean excludedFromStatistics) {
        this.excludedFromStatistics = excludedFromStatistics;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    Set<AdditionalCalendarEvent> getAdditionalCalendarEvents() {
        return additionalCalendarEvents;
    }

    public boolean isRemoteEvent() {
        return remoteEvent;
    }

    public void setRemoteEvent(final boolean remoteEvent) {
        this.remoteEvent = remoteEvent;
    }
}
