package fi.riista.feature.organization.calendar;

import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static fi.riista.util.DateUtil.now;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static fi.riista.feature.organization.calendar.CalendarEventType.VUOSIKOKOUS;
import static fi.riista.util.DateUtil.today;

@RunWith(MockitoJUnitRunner.class)
public class CalendarEventTest {
    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Before
    public void setup()
    {
        doAnswer(invocation -> {
            CalendarEvent savedEvent = invocation.getArgument(0);

            final Date now = now().toDate();

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

    private CalendarEvent createEvent(LocalDate eventDate) {
        CalendarEvent event = new CalendarEvent();
        event.setDate(eventDate.toDate());
        event.setCalendarEventType(VUOSIKOKOUS);

        return event;
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenEventCreatedToday() {
        CalendarEvent event = createEvent(today().minusDays(1));

        calendarEventRepository.save(event);

        assertFalse(event.isLockedAsPastCalendarEvent());
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenEventCreatedYesterday() {
        final LocalDate today = today();
        final LocalDate eventCreateTime = today.minusDays(1);
        MockTimeProvider.mockTime(eventCreateTime.toDate().getTime());
        CalendarEvent event = createEvent(today);

        calendarEventRepository.save(event);

        MockTimeProvider.mockTime(today.toDate().getTime());
        assertFalse(event.isLockedAsPastCalendarEvent());
    }

    @Test
    public void testIsLockedAsPastCalendarEvent_whenEventCreatedTwoDaysPast() {
        final LocalDate today = today();
        final LocalDate eventCreateTime = today.minusDays(2);
        MockTimeProvider.mockTime(eventCreateTime.toDate().getTime());

        CalendarEvent event = createEvent(today.minusDays(1));

        calendarEventRepository.save(event);

        MockTimeProvider.mockTime(today.toDate().getTime());
        assertTrue(event.isLockedAsPastCalendarEvent());
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
