package fi.riista.feature.shootingtest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepositoryCustom.ParticipantSummary;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialDTO;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class ShootingTestCalendarEventDTO {

    private long rhyId;
    private long calendarEventId;
    private Long shootingTestEventId;

    private CalendarEventType calendarEventType;
    private String name;
    private String description;

    private LocalDate date;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime beginTime;

    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime endTime;

    private DateTime lockedTime;

    private VenueDTO venue;

    private List<ShootingTestOfficialDTO> officials;

    private int numberOfAllParticipants;
    private int numberOfParticipantsWithNoAttempts;
    private int numberOfCompletedParticipants;
    private BigDecimal totalPaidAmount;

    private LastModifierDTO lastModifier;

    public static Builder builder() {
        return new Builder();
    }

    // Getters -->

    public long getRhyId() {
        return rhyId;
    }

    public long getCalendarEventId() {
        return calendarEventId;
    }

    public Long getShootingTestEventId() {
        return shootingTestEventId;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public DateTime getLockedTime() {
        return lockedTime;
    }

    public VenueDTO getVenue() {
        return venue;
    }

    public List<ShootingTestOfficialDTO> getOfficials() {
        return officials;
    }

    public int getNumberOfAllParticipants() {
        return numberOfAllParticipants;
    }

    public int getNumberOfParticipantsWithNoAttempts() {
        return numberOfParticipantsWithNoAttempts;
    }

    public int getNumberOfCompletedParticipants() {
        return numberOfCompletedParticipants;
    }

    public BigDecimal getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public LastModifierDTO getLastModifier() {
        return lastModifier;
    }

    // Builder -->

    public static class Builder {

        private CalendarEvent calendarEvent;
        private ShootingTestEvent shootingTestEvent;

        private VenueDTO venue;

        private List<ShootingTestOfficialDTO> officials;
        private ParticipantSummary participantSummary;
        private LastModifierDTO eventLastModifier;

        private static final Comparator<ShootingTestOfficialDTO> responsibleAndNameComparator = Comparator
                .comparing(ShootingTestOfficialDTO::getShootingTestResponsible, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ShootingTestOfficialDTO::getLastName)
                .thenComparing(ShootingTestOfficialDTO::getFirstName);

        public Builder withCalendarEvent(final CalendarEvent calendarEvent) {
            this.calendarEvent = calendarEvent;
            return this;
        }

        public Builder withShootingTestEvent(final ShootingTestEvent shootingTestEvent) {
            this.shootingTestEvent = shootingTestEvent;
            return this;
        }

        public Builder withVenue(final VenueDTO venue) {
            this.venue = venue;
            return this;
        }

        public Builder withOfficials(final List<ShootingTestOfficialDTO> officials) {
            this.officials = officials
                    .stream()
                    .sorted(responsibleAndNameComparator)
                    .collect(Collectors.toList());
            return this;
        }

        public Builder withParticipantSummary(final ParticipantSummary participantSummary) {
            this.participantSummary = participantSummary;
            return this;
        }

        public Builder withLastModifier(final LastModifierDTO eventLastModifier) {
            this.eventLastModifier = eventLastModifier;
            return this;
        }

        public ShootingTestCalendarEventDTO build() {
            requireNonNull(calendarEvent, "calendarEvent is null");
            requireNonNull(venue, "venue is null");

            final ShootingTestCalendarEventDTO dto = new ShootingTestCalendarEventDTO();

            dto.rhyId = calendarEvent.getOrganisation().getId();
            dto.calendarEventId = calendarEvent.getId();

            dto.calendarEventType = calendarEvent.getCalendarEventType();
            dto.name = calendarEvent.getName();
            dto.description = calendarEvent.getDescription();

            dto.date = calendarEvent.getDateAsLocalDate();
            dto.beginTime = calendarEvent.getBeginTime();
            dto.endTime = calendarEvent.getEndTime();

            dto.venue = venue;

            if (shootingTestEvent != null) {
                checkArgument(!F.isNullOrEmpty(officials), "officials must not be null or empty");
                checkArgument(participantSummary != null,
                        "participantSummary must not be null when shootingTestEvent is present");
                checkArgument(eventLastModifier != null,
                        "eventLastModifier must not be null when shootingTestEvent is present");

                dto.shootingTestEventId = shootingTestEvent.getId();
                dto.lockedTime = shootingTestEvent.getLockedTime();

                dto.officials = ImmutableList.copyOf(officials);
                dto.lastModifier = eventLastModifier;

            } else {
                dto.officials = emptyList();
                this.participantSummary = ParticipantSummary.EMPTY;
            }

            dto.numberOfAllParticipants = participantSummary.numberOfAllParticipants;
            dto.numberOfCompletedParticipants = participantSummary.numberOfCompletedParticipants;
            dto.numberOfParticipantsWithNoAttempts = participantSummary.numberOfParticipantsWithNoAttempts;
            dto.totalPaidAmount = participantSummary.totalPaidAmount;

            return dto;
        }
    }
}
