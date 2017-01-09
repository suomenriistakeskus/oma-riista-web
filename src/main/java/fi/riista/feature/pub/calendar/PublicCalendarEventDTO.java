package fi.riista.feature.pub.calendar;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fi.riista.config.jackson.LocalTimeToStringSerializer;

import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class PublicCalendarEventDTO {

    private PublicCalendarEventTypeDTO calendarEventType;
    private String name;
    private String description;
    private LocalDate date;
    private PublicOrganisationDTO organisation;
    private PublicVenueDTO venue;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime beginTime;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime endTime;

    public PublicCalendarEventTypeDTO getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(PublicCalendarEventTypeDTO calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
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

    public PublicOrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(PublicOrganisationDTO organisation) {
        this.organisation = organisation;
    }

    public PublicVenueDTO getVenue() {
        return venue;
    }

    public void setVenue(PublicVenueDTO venue) {
        this.venue = venue;
    }
}
