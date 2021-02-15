package fi.riista.feature.pub.calendar;

import fi.riista.feature.organization.calendar.CalendarEventGroupType;

public class PublicCalendarEventGroupTypeDTO {
    private CalendarEventGroupType calendarEventType;
    private String name;

    public PublicCalendarEventGroupTypeDTO(CalendarEventGroupType calendarEventType, String name) {
        this.calendarEventType = calendarEventType;
        this.name = name;
    }

    public CalendarEventGroupType getCalendarEvenType() {
        return calendarEventType;
    }

    public void setCalendarEventType(final CalendarEventGroupType calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}