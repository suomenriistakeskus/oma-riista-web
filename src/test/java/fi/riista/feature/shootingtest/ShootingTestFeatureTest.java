package fi.riista.feature.shootingtest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.shootingtest.ShootingTest.DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ShootingTestFeatureTest extends EmbeddedDatabaseTest implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestFeature feature;

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByOccupationType() {
        final LocalDate today = today();

        withRhy(rhy -> {

            OccupationType
                    .getApplicableTypes(RHY)
                    .stream()
                    .filter(occType -> occType != AMPUMAKOKEEN_VASTAANOTTAJA && occType != TOIMINNANOHJAAJA)
                    .forEach(occupationType -> withPerson(person -> {

                        person.setRhyMembership(rhy);
                        model().newOccupation(rhy, person, occupationType);

                        openEvent(rhy, today);
                        openEvent(rhy, today.minusDays(1));

                        onSavedAndAuthenticated(createUser(person), () -> {
                            assertEmpty(feature.listRecentCalendarEventsForAllRhys());
                        });
                    }));
        });
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByRhy() {
        final LocalDate today = today();

        withRhyAndShootingTestOfficial((rhy, officialPerson) -> withRhy(rhy2 -> {

            model().newOccupation(rhy2, officialPerson, TOIMINNANOHJAAJA);

            final CalendarEvent unopenedCalendarEvent = model().newCalendarEvent(rhy, AMPUMAKOE, today);
            final CalendarEvent unopenedCalendarEvent2 = model().newCalendarEvent(rhy2, AMPUMAKOE, today);

            final ShootingTestEvent event = openEvent(rhy, today);
            final ShootingTestEvent event2 = openEvent(rhy2, today);

            // Create events for different RHYs.
            model().newCalendarEvent(model().newRiistanhoitoyhdistys(), AMPUMAKOE, today);
            openEvent(model().newRiistanhoitoyhdistys(), today);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                final List<ShootingTestCalendarEventDTO> list = feature.listRecentCalendarEventsForAllRhys();

                assertEquals(
                        F.getUniqueIds(unopenedCalendarEvent, unopenedCalendarEvent2, event.getCalendarEvent(),
                                event2.getCalendarEvent()),
                        collectCalendarEventIds(list));
            });
        }));
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByDate_forShootingTestOfficial() {
        withRhyAndShootingTestOfficial(this::testFilteringByDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotAcquireWrongTypeOfEvent() {
        withRhyAndShootingTestOfficial((rhy, official) -> {

            final CalendarEvent calendarEvent = model().newCalendarEvent(rhy, CalendarEventType.VUOSIKOKOUS, today());

            onSavedAndAuthenticated(createUser(official), () -> {

                feature.getCalendarEvent(calendarEvent.getId());
                fail("Should have thrown an exception");
            });
        });
    }

    @Test
    public void testOfficialCanAcquireShootingTest() {
        withRhyAndShootingTestOfficial((rhy, official) -> {

            final CalendarEvent calendarEvent = model().newCalendarEvent(rhy, CalendarEventType.AMPUMAKOE, today());

            onSavedAndAuthenticated(createUser(official), () -> {

                final ShootingTestCalendarEventDTO event = feature.getCalendarEvent(calendarEvent.getId());
                assertEquals(event.getCalendarEventId(), (long) calendarEvent.getId());
            });
        });
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByDate_forCoordinator() {
        withRhyAndCoordinator(this::testFilteringByDate);
    }

    private void testFilteringByDate(final Riistanhoitoyhdistys rhy, final Person person) {
        final LocalDate today = today();
        final LocalDate tomorrow = today.plusDays(1);
        final LocalDate earliestValidDate = today.minus(DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL);

        final CalendarEvent calendarEvent1 = model().newCalendarEvent(rhy, AMPUMAKOE, today);
        final CalendarEvent calendarEvent2 = model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, earliestValidDate);

        final ShootingTestEvent event1 = openEvent(rhy, today);
        final ShootingTestEvent event2 = openEvent(rhy, today.minusDays(1));
        final ShootingTestEvent event3 = openEvent(rhy, earliestValidDate);

        // Events having date out of accepted range should not be included in the results.
        model().newCalendarEvent(rhy, AMPUMAKOE, tomorrow);
        model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, tomorrow);
        model().newCalendarEvent(rhy, AMPUMAKOE, earliestValidDate.minusDays(1));
        model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, earliestValidDate.minusDays(1));
        openEvent(rhy, earliestValidDate.minusDays(1));

        onSavedAndAuthenticated(createUser(person), () -> {

            final List<ShootingTestCalendarEventDTO> list = feature.listRecentCalendarEventsForAllRhys();

            assertEquals(
                    F.getUniqueIds(calendarEvent1, calendarEvent2, event1.getCalendarEvent(), event2.getCalendarEvent(),
                            event3.getCalendarEvent()),
                    collectCalendarEventIds(list));
        });
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyNonShootingTestEventTypesAreExcluded() {
        withRhyAndShootingTestOfficial((rhy, official) -> {

            final SystemUser user = createUser(official);

            CalendarEventType.activeCalendarEventTypes().stream()
                    .filter(calendarEventType -> !calendarEventType.isShootingTest())
                    .forEach(calendarEventType -> {

                        model().newCalendarEvent(rhy, calendarEventType);

                        onSavedAndAuthenticated(user, () -> assertEmpty(feature.listRecentCalendarEventsForAllRhys()));
                    });
        });
    }

    private static Set<Long> collectCalendarEventIds(final Collection<ShootingTestCalendarEventDTO> coll) {
        return F.mapNonNullsToSet(coll, ShootingTestCalendarEventDTO::getCalendarEventId);
    }
}
