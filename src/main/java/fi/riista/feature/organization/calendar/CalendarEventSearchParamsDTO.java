package fi.riista.feature.organization.calendar;

import fi.riista.feature.pub.calendar.PublicCalendarEventSearchDTO;
import org.joda.time.LocalDate;

import java.util.Optional;

public class CalendarEventSearchParamsDTO {

    private String areaId;
    private String rhyId;
    private CalendarEventType calendarEventType;
    private LocalDate begin;
    private LocalDate end;
    private Boolean onlyPubliclyVisible;
    private Boolean onlyPublicEvents;
    private int limit;
    private int offset;

    // Only for testing
    CalendarEventSearchParamsDTO() {
        this.limit = 20;
        this.offset = 0;
    }

    public CalendarEventSearchParamsDTO(final PublicCalendarEventSearchDTO params,
                                        final int limit,
                                        final int offset) {
        this.areaId = params.getAreaId();
        this.rhyId = params.getRhyId();
        this.calendarEventType = params.getCalendarEventType();
        this.begin = params.getBegin();
        this.end = params.getEnd();
        this.onlyPubliclyVisible = true;
        this.onlyPublicEvents = true;
        this.limit = limit;
        this.offset = offset;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(final String areaId) {
        this.areaId = areaId;
    }

    public String getRhyId() {
        return rhyId;
    }

    public void setRhyId(final String rhyId) {
        this.rhyId = rhyId;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(final CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(final LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(final LocalDate end) {
        this.end = end;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public boolean getOnlyPubliclyVisible() {
        return Boolean.TRUE.equals(onlyPubliclyVisible);
    }

    public void setOnlyPubliclyVisible(final Boolean onlyPubliclyVisible) {
        this.onlyPubliclyVisible = onlyPubliclyVisible;
    }

    public boolean getOnlyPublicEvents() {
        return Boolean.TRUE.equals(onlyPublicEvents);
    }

    public void setOnlyPublicEvents(final Boolean onlyPublicEvents) {
        this.onlyPublicEvents = onlyPublicEvents;
    }
}
