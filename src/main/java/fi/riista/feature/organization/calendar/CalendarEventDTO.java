package fi.riista.feature.organization.calendar;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.util.DateUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class CalendarEventDTO extends BaseEntityDTO<Long> {

    public static CalendarEventDTO create(CalendarEvent calendarEvent) {
        CalendarEventDTO dto = new CalendarEventDTO();
        dto.setId(calendarEvent.getId());
        dto.setRev(calendarEvent.getConsistencyVersion());

        dto.setName(calendarEvent.getName());
        dto.setCalendarEventType(calendarEvent.getCalendarEventType());
        dto.setDate(DateUtil.toLocalDateNullSafe(calendarEvent.getDate()));
        dto.setBeginTime(calendarEvent.getBeginTime());
        dto.setEndTime(calendarEvent.getEndTime());
        dto.setDescription(calendarEvent.getDescription());
        dto.setOrganisation(OrganisationDTO.create(calendarEvent.getOrganisation()));
        dto.setVenue(VenueDTO.create(calendarEvent.getVenue()));
        return dto;
    }

    private Long id;
    private Integer rev;

    private CalendarEventType calendarEventType;
    private LocalDate date;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime beginTime;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime endTime;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    @Valid
    private OrganisationDTO organisation;

    @Valid
    private VenueDTO venue;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
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

    public OrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationDTO organisation) {
        this.organisation = organisation;
    }

    public VenueDTO getVenue() {
        return venue;
    }

    public void setVenue(VenueDTO venue) {
        this.venue = venue;
    }

}
