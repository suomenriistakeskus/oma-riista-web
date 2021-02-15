package fi.riista.feature.organization.calendar;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchDTO;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.EnumSet;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class CalendarEventSearchParamsDTO {

    private String areaId;
    private Collection<String>  rhyIds;
    private ImmutableSet<CalendarEventType> calendarEventTypes;
    private LocalDate begin;
    private LocalDate end;
    private Boolean onlyPubliclyVisible;
    private Boolean onlyPublicEvents;
    private Integer limit;
    private int offset;

    // Only for testing
    CalendarEventSearchParamsDTO() {
        this.limit = 20;
        this.offset = 0;
    }

    public CalendarEventSearchParamsDTO(final String areaId,
                                        final Collection<String> rhyIds,
                                        final ImmutableSet<CalendarEventType> calendarEventTypes,
                                        final LocalDate begin,
                                        final LocalDate end,
                                        final Boolean onlyPubliclyVisible,
                                        final Boolean onlyPublicEvents,
                                        final Integer limit,
                                        final int offset) {
        this.areaId = areaId;
        this.rhyIds = rhyIds;
        this.calendarEventTypes = calendarEventTypes;
        this.begin = begin;
        this.end = end;
        this.onlyPubliclyVisible = onlyPubliclyVisible;
        this.onlyPublicEvents = onlyPublicEvents;
        this.limit = limit;
        this.offset = offset;
    }

    public CalendarEventSearchParamsDTO(final PublicCalendarEventSearchDTO params,
                                        final int limit,
                                        final int offset) {
        this.areaId = params.getAreaId();
        this.rhyIds = ofNullable(params.getRhyIds()).orElse(emptyList());
        this.calendarEventTypes = CalendarEventGroupType.getCalenderEventTypes(params.getCalendarEventType());
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

    public Collection<String> getRhyIds() {
        return ofNullable(rhyIds).orElse(emptyList());
    }

    public void setRhyIds(final Collection<String> rhyIds) {
        this.rhyIds = rhyIds;
    }

    public ImmutableSet<CalendarEventType> getCalendarEventTypes() {
        return calendarEventTypes == null ? immutableEnumSet(EnumSet.noneOf(CalendarEventType.class)) : calendarEventTypes;
    }

    public void setCalendarEventTypes(final ImmutableSet<CalendarEventType> calendarEventTypes) {
        this.calendarEventTypes = calendarEventTypes;
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

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
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
