package fi.riista.feature.organization.calendar;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.feature.organization.address.Address;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AdditionalCalendarEventDTO {

    public static AdditionalCalendarEventDTO create(final AdditionalCalendarEvent event,
                                                    final Venue venue,
                                                    final Address venueAddress) {
        final AdditionalCalendarEventDTO dto = new AdditionalCalendarEventDTO();

        dto.setId(event.getId());
        dto.setDate(event.getDateAsLocalDate());
        dto.setBeginTime(event.getBeginTime());
        dto.setEndTime(event.getEndTime());

        dto.setVenue(VenueDTO.create(venue, venueAddress));

        return dto;
    }

    private Long id;

    @NotNull
    private LocalDate date;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @NotNull
    private LocalTime beginTime;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime endTime;

    @Valid
    @NotNull
    private VenueDTO venue;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public VenueDTO getVenue() {
        return venue;
    }

    public void setVenue(final VenueDTO venue) {
        this.venue = venue;
    }
}
