package fi.riista.feature.shootingtest;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.shootingtest.official.ShootingTestOfficial;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.shootingtest.ShootingTest.DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class ShootingTestEvent extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @JoinColumn(name = "calendar_event_id", unique = true, nullable = false, updatable = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private CalendarEvent calendarEvent;

    @Column
    private DateTime lockedTime;

    @OneToMany(mappedBy = "shootingTestEvent")
    private Set<ShootingTestOfficial> officials = new HashSet<>();

    @OneToMany(mappedBy = "shootingTestEvent")
    private Set<ShootingTestParticipant> participants = new HashSet<>();

    ShootingTestEvent() {
    }

    public ShootingTestEvent(@Nonnull final CalendarEvent calendarEvent) {
        checkArgument(calendarEvent.getCalendarEventType().isShootingTest());
        this.calendarEvent = requireNonNull(calendarEvent);

        assertState(!calendarEvent.getDateAsLocalDate().isAfter(today()),
                "Cannot open shooting test event into the future");
    }

    public boolean hasOccurredWithinLastWeek() {
        final LocalDate eventDate = calendarEvent.getDateAsLocalDate();
        final LocalDate today = today();

        return !today.isBefore(eventDate)
                && !Days.daysBetween(eventDate, today).isGreaterThan(DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL);
    }

    public boolean isClosed() {
        return lockedTime != null;
    }

    public void close() {
        assertOpen("Shooting test event is already closed");
        this.lockedTime = DateUtil.now();
    }

    public void reopen() {
        assertState(isClosed(), "Shooting test event is not closed");
        this.lockedTime = null;
    }

    public void assertOpen(final String errorMessage) {
        assertState(!isClosed(), errorMessage);
    }

    private static void assertState(final boolean expectedCondition, final String errorMessage) {
        if (!expectedCondition) {
            throw new IllegalShootingTestEventStateException(errorMessage);
        }
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "shooting_test_event_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public DateTime getLockedTime() {
        return lockedTime;
    }

    Set<ShootingTestOfficial> getOfficials() {
        return officials;
    }

    Set<ShootingTestParticipant> getParticipants() {
        return participants;
    }
}
