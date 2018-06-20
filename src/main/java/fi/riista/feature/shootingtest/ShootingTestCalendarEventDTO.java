package fi.riista.feature.shootingtest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.VenueDTO;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.util.List;

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

    public long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final long rhyId) {
        this.rhyId = rhyId;
    }

    public long getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(final long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public Long getShootingTestEventId() {
        return shootingTestEventId;
    }

    public void setShootingTestEventId(final Long shootingTestEventId) {
        this.shootingTestEventId = shootingTestEventId;
    }

    public CalendarEventType getCalendarEventType() {
        return calendarEventType;
    }

    public void setCalendarEventType(final CalendarEventType calendarEventType) {
        this.calendarEventType = calendarEventType;
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

    public DateTime getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(final DateTime lockedTime) {
        this.lockedTime = lockedTime;
    }

    public VenueDTO getVenue() {
        return venue;
    }

    public void setVenue(final VenueDTO venue) {
        this.venue = venue;
    }

    public List<ShootingTestOfficialDTO> getOfficials() {
        return officials;
    }

    public void setOfficials(final List<ShootingTestOfficialDTO> officials) {
        this.officials = officials;
    }

    public int getNumberOfAllParticipants() {
        return numberOfAllParticipants;
    }

    public void setNumberOfAllParticipants(final int numberOfAllParticipants) {
        this.numberOfAllParticipants = numberOfAllParticipants;
    }

    public int getNumberOfParticipantsWithNoAttempts() {
        return numberOfParticipantsWithNoAttempts;
    }

    public void setNumberOfParticipantsWithNoAttempts(final int numberOfParticipantsWithNoAttempts) {
        this.numberOfParticipantsWithNoAttempts = numberOfParticipantsWithNoAttempts;
    }

    public int getNumberOfCompletedParticipants() {
        return numberOfCompletedParticipants;
    }

    public void setNumberOfCompletedParticipants(final int numberOfCompletedParticipants) {
        this.numberOfCompletedParticipants = numberOfCompletedParticipants;
    }

    public BigDecimal getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(final BigDecimal totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }
}
