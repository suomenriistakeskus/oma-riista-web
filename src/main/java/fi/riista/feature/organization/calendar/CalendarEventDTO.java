package fi.riista.feature.organization.calendar;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class CalendarEventDTO extends BaseEntityDTO<Long> {

    public static CalendarEventDTO create(final CalendarEvent event,
                                          final Organisation organisation,
                                          final Venue venue,
                                          final Address venueAddress,
                                          final boolean isAssociatedWithShootingTestEvent) {

        final CalendarEventDTO dto = new CalendarEventDTO();
        DtoUtil.copyBaseFields(event, dto);

        dto.setName(event.getName());
        dto.setCalendarEventType(event.getCalendarEventType());
        dto.setDate(DateUtil.toLocalDateNullSafe(event.getDate()));
        dto.setBeginTime(event.getBeginTime());
        dto.setEndTime(event.getEndTime());
        dto.setDescription(event.getDescription());

        dto.setOrganisation(OrganisationDTO.create(organisation));
        dto.setVenue(VenueDTO.create(venue, venueAddress));

        dto.setAssociatedWithShootingTestEvent(isAssociatedWithShootingTestEvent);

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

    private Boolean associatedWithShootingTestEvent;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(final CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
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

    public OrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(final OrganisationDTO organisation) {
        this.organisation = organisation;
    }

    public VenueDTO getVenue() {
        return venue;
    }

    public void setVenue(final VenueDTO venue) {
        this.venue = venue;
    }

    public Boolean getAssociatedWithShootingTestEvent() {
        return associatedWithShootingTestEvent;
    }

    public void setAssociatedWithShootingTestEvent(final Boolean associatedWithShootingTestEvent) {
        this.associatedWithShootingTestEvent = associatedWithShootingTestEvent;
    }
}
