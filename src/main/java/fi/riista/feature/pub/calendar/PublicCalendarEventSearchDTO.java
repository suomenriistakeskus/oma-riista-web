package fi.riista.feature.pub.calendar;

import fi.riista.feature.organization.calendar.CalendarEventType;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

public class PublicCalendarEventSearchDTO {

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String areaId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyId;

    private CalendarEventType calendarEventType;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate begin;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end;

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getRhyId() {
        return rhyId;
    }

    public void setRhyId(String rhyId) {
        this.rhyId = rhyId;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "CalendarEventSearchParams{" +
                "areaId=" + areaId +
                ", rhyId=" + rhyId +
                ", calendarEventType=" + calendarEventType +
                ", begin=" + begin +
                ", end=" + end +
                '}';
    }
}
