package fi.riista.feature.shootingtest.expiry;

import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShootingTestEndOfYearExpiryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ShootingTestEndOfYearExpiryFeature feature;

    @Test
    public void testGetExpiredShootingTests() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys();

        final CalendarEvent calendarEvent1 = model().newCalendarEvent(rhy, AMPUMAKOE, new LocalDate(2019, 1, 1));
        model().newShootingTestEvent(calendarEvent1);
        final CalendarEvent calendarEvent2 = model().newCalendarEvent(rhy, JOUSIAMPUMAKOE, new LocalDate(2019, 12, 31));
        model().newShootingTestEvent(calendarEvent2);

        final CalendarEvent calendarEvent3 = model().newCalendarEvent(rhy2, AMPUMAKOE, new LocalDate(2019, 6, 1));
        model().newShootingTestEvent(calendarEvent3);

        final CalendarEvent calendarEvent4 = model().newCalendarEvent(rhy3, AMPUMAKOE, new LocalDate(2020, 1, 1));
        model().newShootingTestEvent(calendarEvent4);
        final CalendarEvent calendarEvent5 = model().newCalendarEvent(rhy3, AMPUMAKOE, new LocalDate(2019, 2, 28));
        final ShootingTestEvent shootingTestEvent = model().newShootingTestEvent(calendarEvent5);
        shootingTestEvent.close();

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final Map<Long, List<ShootingTestEndOfYearExpiryDTO>> expiryMap = feature.getOpenShootingTest(2019);

            assertEquals(2, expiryMap.keySet().size());

            assertTrue(expiryMap.keySet().contains(rhy.getId()));
            assertTrue(expiryMap.keySet().contains(rhy2.getId()));

            final List<ShootingTestEndOfYearExpiryDTO> rhyDtos = expiryMap.get(rhy.getId());
            assertEquals(2, rhyDtos.size());

            final List<ShootingTestEndOfYearExpiryDTO> rhy2Dtos = expiryMap.get(rhy2.getId());
            assertEquals(1, rhy2Dtos.size());
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetExpiredShootingTests_unauthorized() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final CalendarEvent calendarEvent1 = model().newCalendarEvent(rhy, AMPUMAKOE, new LocalDate(2019, 1, 1));
        model().newShootingTestEvent(calendarEvent1);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.getOpenShootingTest(2019);
        });
    }
}
