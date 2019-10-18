package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Access(value = AccessType.FIELD)
public class AdditionalCalendarEvent extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @Column(nullable = false)
    private Date date;

    @NotNull
    @Column(nullable = false)
    private LocalTime beginTime;

    @Column
    private LocalTime endTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CalendarEvent calendarEvent;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Venue venue;

    public AdditionalCalendarEvent() {}

    public AdditionalCalendarEvent(final Date date,
                                   final LocalTime beginTime,
                                   final LocalTime endTime,
                                   final CalendarEvent calendarEvent,
                                   final Venue venue) {
        setDate(date);
        setBeginTime(beginTime);
        setEndTime(endTime);
        setCalendarEvent(calendarEvent);
        setVenue(venue);

    }

    public LocalDate getDateAsLocalDate() {
        return DateUtil.toLocalDateNullSafe(date);
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "additional_calendar_event_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
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

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public void setCalendarEvent(final CalendarEvent calendarEvent) {
        CriteriaUtils.updateInverseCollection(CalendarEvent_.additionalCalendarEvents, this, this.calendarEvent, calendarEvent);
        this.calendarEvent = calendarEvent;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(final Venue venue) {
        this.venue = venue;
    }
}
