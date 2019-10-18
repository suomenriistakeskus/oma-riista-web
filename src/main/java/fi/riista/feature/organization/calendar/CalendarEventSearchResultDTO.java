package fi.riista.feature.organization.calendar;

import org.joda.time.LocalTime;
import java.util.Date;

public class CalendarEventSearchResultDTO {
    private Long calendarEventId;
    private Long additionalCalendarEventId;
    private CalendarEventType calendarEventType;
    private String name;
    private String description;
    private Date date;
    private LocalTime beginTime;
    private LocalTime endTime;
    private Long organisationId;
    private Long venueId;

    public CalendarEventSearchResultDTO(final Long calendarEventId,
                                        final Long additionalCalendarEventId,
                                        final CalendarEventType calendarEventType,
                                        final String name,
                                        final String description,
                                        final Date date,
                                        final LocalTime beginTime,
                                        final LocalTime endTime,
                                        final Long organisationId,
                                        final Long venueId) {
        this.calendarEventId = calendarEventId;
        this.additionalCalendarEventId = additionalCalendarEventId;
        this.calendarEventType = calendarEventType;
        this.name = name;
        this.description = description;
        this.date = date;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.organisationId = organisationId;
        this.venueId = venueId;
    }

    public Long getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(final Long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public Long getAdditionalCalendarEventId() {
        return additionalCalendarEventId;
    }

    public void setAdditionalCalendarEventId(final Long additionalCalendarEventId) {
        this.additionalCalendarEventId = additionalCalendarEventId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(final Long organisationId) {
        this.organisationId = organisationId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(final Long venueId) {
        this.venueId = venueId;
    }
}
