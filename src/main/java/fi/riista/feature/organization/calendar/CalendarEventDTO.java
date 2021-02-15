package fi.riista.feature.organization.calendar;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.organization.calendar.CalendarEventType.additionalEventsAllowedTypes;

public class CalendarEventDTO extends BaseEntityDTO<Long> {
    public static CalendarEventDTO create(final CalendarEvent event,
                                          final Organisation organisation,
                                          final Venue venue,
                                          final Address venueAddress,
                                          final boolean lockedAsPastCalendarEvent,
                                          final boolean lockedAsPastStatistics,
                                          final List<AdditionalCalendarEventDTO> additionalCalendarEvents) {
        final CalendarEventDTO dto = new CalendarEventDTO();
        DtoUtil.copyBaseFields(event, dto);

        dto.setName(event.getName());
        dto.setCalendarEventType(event.getCalendarEventType());
        dto.setDate(event.getDateAsLocalDate());
        dto.setBeginTime(event.getBeginTime());
        dto.setEndTime(event.getEndTime());
        dto.setDescription(event.getDescription());

        dto.setOrganisation(OrganisationDTO.create(organisation));
        dto.setVenue(VenueDTO.create(venue, venueAddress));

        dto.setLockedAsPastCalendarEvent(lockedAsPastCalendarEvent);

        dto.setPublicVisibility(event.getPublicVisibility());

        dto.setExcludedFromStatistics(event.getExcludedFromStatistics());

        dto.setParticipants(event.getParticipants());

        dto.setLockedAsPastStatistics(lockedAsPastStatistics);

        dto.setAdditionalCalendarEvents(additionalCalendarEvents);

        dto.setRemoteEvent(event.isRemoteEvent());

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

    private Boolean lockedAsPastCalendarEvent;

    private boolean publicVisibility;

    private boolean excludedFromStatistics;

    @Min(0)
    private Integer participants;

    private Boolean lockedAsPastStatistics;

    @Valid
    private List<AdditionalCalendarEventDTO> additionalCalendarEvents;

    private boolean remoteEvent;

    @AssertTrue
    public boolean isAdditionalEventsInfoValid() {
        return F.isNullOrEmpty(additionalCalendarEvents)
                || additionalEventsAllowedTypes().contains(calendarEventType);
    }

    @AssertTrue
    public boolean isValidRemoteEvent() {
        return !remoteEvent || calendarEventType.isRemoteEventAllowed();
    }

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

    public Boolean getLockedAsPastCalendarEvent() {
        return lockedAsPastCalendarEvent;
    }

    public void setLockedAsPastCalendarEvent(final Boolean lockedAsPastCalendarEvent) {
        this.lockedAsPastCalendarEvent = lockedAsPastCalendarEvent;
    }

    public boolean getPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(final boolean publicVisibility) {
        this.publicVisibility = publicVisibility;
    }

    public boolean getExcludedFromStatistics() {
        return excludedFromStatistics;
    }

    public void setExcludedFromStatistics(final boolean excludedFromStatistics) {
        this.excludedFromStatistics = excludedFromStatistics;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public Boolean getLockedAsPastStatistics() {
        return lockedAsPastStatistics;
    }

    public void setLockedAsPastStatistics(Boolean lockedAsPastStatistics) {
        this.lockedAsPastStatistics = lockedAsPastStatistics;
    }

    public List<AdditionalCalendarEventDTO> getAdditionalCalendarEvents() {
        return additionalCalendarEvents == null ? Collections.emptyList() : additionalCalendarEvents;
    }

    public void setAdditionalCalendarEvents(final List<AdditionalCalendarEventDTO> additionalCalendarEvents) {
        this.additionalCalendarEvents = additionalCalendarEvents;
    }

    public boolean isRemoteEvent() {
        return remoteEvent;
    }

    public void setRemoteEvent(final boolean remoteEvent) {
        this.remoteEvent = remoteEvent;
    }
}
