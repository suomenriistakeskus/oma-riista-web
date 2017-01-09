package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.Organisation;
import org.joda.time.LocalTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

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

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
