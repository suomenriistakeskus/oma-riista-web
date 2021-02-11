package fi.riista.feature.organization;

import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static fi.riista.feature.organization.calendar.CalendarEventType.VUOSIKOKOUS;
import static org.junit.Assert.assertEquals;

public class OrganisationCrudFeatureTest extends EmbeddedDatabaseTest {
    @Resource
    private OrganisationCrudFeature organisationCrudFeature;

    private List<CalendarEvent> events = new ArrayList<>();
    private List<CalendarEvent> eventsForAnotherOrg = new ArrayList<>();
    private List<CalendarEvent> eventsForPastYear = new ArrayList<>();
    private static int NR_OF_EVENTS = 5;
    private static int NR_OF_EVENTS_PAST_YEAR = NR_OF_EVENTS + 2;

    private static LocalDate eventDate = new LocalDate(2019, 9, 16);
    private static LocalDate eventDatePastYear = new LocalDate(2018, 9, 16);

    private void createEventsAndVenues(final Riistanhoitoyhdistys rhy) {
        final Organisation org2 = model().newRiistanhoitoyhdistys();

        persistInNewTransaction();

        for (int i = 0; i < NR_OF_EVENTS; i++) {
            events.add(model().newCalendarEvent(rhy, VUOSIKOKOUS, eventDate.plusDays(i)));
            eventsForAnotherOrg.add(model().newCalendarEvent(org2, VUOSIKOKOUS, eventDate.plusDays(i)));
            eventsForPastYear.add(model().newCalendarEvent(rhy, VUOSIKOKOUS, eventDatePastYear.plusDays(i)));
        }

        eventsForAnotherOrg.add(model().newCalendarEvent(org2, VUOSIKOKOUS, eventDate.plusDays(NR_OF_EVENTS)));

        eventsForPastYear.add(model().newCalendarEvent(rhy, VUOSIKOKOUS, eventDatePastYear.plusDays(NR_OF_EVENTS)));
        eventsForPastYear.add(model().newCalendarEvent(rhy, VUOSIKOKOUS, eventDatePastYear.plusDays(NR_OF_EVENTS + 1)));
    }

    @Test
    public void testListEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            createEventsAndVenues(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<CalendarEventDTO> events = organisationCrudFeature.listEvents(rhy.getId());

                assertEquals(NR_OF_EVENTS + NR_OF_EVENTS_PAST_YEAR, events.size());
            });
        });
    }

    @Test
    public void testListEventsByYear() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            createEventsAndVenues(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<CalendarEventDTO> events = organisationCrudFeature
                        .listEventsByYear(rhy.getId(), eventDatePastYear.getYear());
                assertEquals(NR_OF_EVENTS_PAST_YEAR, events.size());
            });
        });
    }
}
