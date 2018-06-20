package fi.riista.feature.shootingtest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.shootingtest.ShootingTestEvent.DAYS_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class ShootingTestFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ShootingTestFeature feature;

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByOccupationType() {
        final LocalDate today = today();

        withRhy(rhy -> {

            OccupationType.getApplicableTypes(RHY)
                    .stream()
                    .filter(occType -> occType != AMPUMAKOKEEN_VASTAANOTTAJA)
                    .forEach(occupationType -> withPerson(person -> {

                        person.setRhyMembership(rhy);
                        model().newOccupation(rhy, person, occupationType);

                        model().newShootingTestEvent(rhy, today);
                        model().newShootingTestEvent(rhy, today.minusDays(1));

                        onSavedAndAuthenticated(createUser(person), () -> {
                            assertEmpty(feature.listRecentCalendarEventsForAllRhys());
                        });
                    }));
        });
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByRhy() {
        final LocalDate today = today();

        withRhyAndShootingTestOfficial((rhy, official) -> {

            final CalendarEvent calendarEvent = model().newCalendarEvent(rhy, AMPUMAKOE, today);
            final ShootingTestEvent event = model().newShootingTestEvent(rhy, today);

            // Create event for different RHY.
            model().newCalendarEvent(model().newRiistanhoitoyhdistys(), JOUSIAMPUMAKOE, today);
            model().newShootingTestEvent(model().newRiistanhoitoyhdistys(), today);

            onSavedAndAuthenticated(createUser(official), () -> {

                final List<ShootingTestCalendarEventDTO> list = feature.listRecentCalendarEventsForAllRhys();

                assertEquals(F.getUniqueIds(event.getCalendarEvent(), calendarEvent), collectCalendarEventIds(list));
            });
        });
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByDate() {
        final LocalDate today = today();
        final LocalDate tomorrow = today.plusDays(1);
        final LocalDate oldestValidDate = today.minus(DAYS_UPDATEABLE_BY_OFFICIAL);

        withRhyAndShootingTestOfficial((rhy, official) -> {

            final CalendarEvent calendarEvent1 = model().newCalendarEvent(rhy, AMPUMAKOE, today);
            final CalendarEvent calendarEvent2 = model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, oldestValidDate);

            final ShootingTestEvent event1 = model().newShootingTestEvent(rhy, today);
            final ShootingTestEvent event2 = model().newShootingTestEvent(rhy, today.minusDays(1));
            final ShootingTestEvent event3 = model().newShootingTestEvent(rhy, oldestValidDate);

            // Events having date out of accepted range should not be included in the results.
            model().newCalendarEvent(rhy, AMPUMAKOE, tomorrow);
            model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, tomorrow);
            model().newCalendarEvent(rhy, AMPUMAKOE, oldestValidDate.minusDays(1));
            model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, oldestValidDate.minusDays(1));
            model().newShootingTestEvent(rhy, oldestValidDate.minusDays(1));

            onSavedAndAuthenticated(createUser(official), () -> {

                final List<ShootingTestCalendarEventDTO> list = feature.listRecentCalendarEventsForAllRhys();

                assertEquals(
                        F.getUniqueIds(calendarEvent1, calendarEvent2, event1.getCalendarEvent(),
                                event2.getCalendarEvent(), event3.getCalendarEvent()),
                        collectCalendarEventIds(list));
            });
        });
    }

    @Test
    public void testListRecentCalendarEventsForAllRhys_verifyFilteringByCalendarEventType() {
        withRhyAndShootingTestOfficial((rhy, official) -> {

            final SystemUser user = createUser(official);

            Arrays.stream(CalendarEventType.values())
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
