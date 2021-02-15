package fi.riista.feature.organization.calendar;

import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.VUOSIKOKOUS;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class CalendarEventTest {
    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Before
    public void setup()
    {
        doAnswer(invocation -> {
            CalendarEvent savedEvent = invocation.getArgument(0);

            final DateTime now = now();

            savedEvent.getLifecycleFields().setCreationTime(now);
            savedEvent.getLifecycleFields().setModificationTime(now);

            savedEvent.getAuditFields().setCreatedByUserId(Long.valueOf(1));
            savedEvent.getAuditFields().setModifiedByUserId(Long.valueOf(1));

            return savedEvent;
        }).when(calendarEventRepository).save(isA(CalendarEvent.class));
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    private static CalendarEvent createEvent(LocalDate eventDate) {
        CalendarEvent event = new CalendarEvent();
        event.setDate(eventDate.toDate());
        event.setCalendarEventType(VUOSIKOKOUS);

        return event;
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenEventInPastWithParticipants() {
        CalendarEvent event = createEvent(today().minusDays(1));
        event.setParticipants(1);

        calendarEventRepository.save(event);

        assertTrue(event.isLockedAsPastCalendarEvent());
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenEventInPastWithoutParticipants() {
        CalendarEvent event = createEvent(today().minusDays(1));

        calendarEventRepository.save(event);

        assertFalse(event.isLockedAsPastCalendarEvent());
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenShootingTestEvent() {
        CalendarEvent event = createEvent(today().minusDays(1));
        event.setCalendarEventType(AMPUMAKOE);

        calendarEventRepository.save(event);

        assertFalse(event.isLockedAsPastCalendarEvent());
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenBowShootingTestEvent() {
        CalendarEvent event = createEvent(today().minusDays(1));
        event.setCalendarEventType(JOUSIAMPUMAKOE);

        calendarEventRepository.save(event);

        assertFalse(event.isLockedAsPastCalendarEvent());
    }

    @Test
    public void testIsLockedAsPastStatisticsEvent_whenEventInCurrentYear() {
        CalendarEvent event = createEvent(today());

        calendarEventRepository.save(event);

        assertFalse(event.isLockedAsPastStatistics());
    }

    @Test
    public void testIsLockedAsPastStatisticsEvent_whenEventInPastYearAndPastYearStatisticsOpen() {
        final LocalDate today = today();
        CalendarEvent event = createEvent(today.minusYears(1));

        calendarEventRepository.save(event);

        final LocalDate openPastStatisticsDate = new LocalDate(today.getYear(), 1, 15);
        MockTimeProvider.mockTime(openPastStatisticsDate.toDate().getTime());
        assertFalse(event.isLockedAsPastStatistics());
    }

    @Test
    public void testIsLockedAsPastStatisticsEvent_whenEventInPastYearAndPastYearStatisticsClosed() {
        final LocalDate today = today();
        CalendarEvent event = createEvent(today.minusYears(1));

        calendarEventRepository.save(event);

        final LocalDate closedPastStatisticsDate = new LocalDate(today.getYear(), 1, 16);
        MockTimeProvider.mockTime(closedPastStatisticsDate.toDate().getTime());
        assertTrue(event.isLockedAsPastStatistics());
    }

    @Test
    public void testIsLockedAsPastStatisticsEvent_whenInTwoYearsPast() {
        CalendarEvent event = createEvent(today().minusYears(2));

        calendarEventRepository.save(event);

        assertTrue(event.isLockedAsPastStatistics());
    }
}
