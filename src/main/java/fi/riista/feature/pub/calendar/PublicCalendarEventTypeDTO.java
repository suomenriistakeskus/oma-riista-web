package fi.riista.feature.pub.calendar;

import fi.riista.feature.organization.calendar.CalendarEventType;

public class PublicCalendarEventTypeDTO {
    private CalendarEventType calendarEventType;
    private String name;

    public PublicCalendarEventTypeDTO(CalendarEventType calendarEventType, String name) {
        this.calendarEventType = calendarEventType;
        this.name = name;
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
}
