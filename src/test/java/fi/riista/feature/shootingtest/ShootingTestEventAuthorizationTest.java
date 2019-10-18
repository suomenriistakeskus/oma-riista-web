package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import static fi.riista.feature.organization.calendar.CalendarEventType.shootingTestTypes;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.shootingtest.ShootingTest.DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.feature.shootingtest.ShootingTestEventAuthorization.ShootingTestEventPermission.ASSIGN_OFFICIALS;
import static fi.riista.feature.shootingtest.ShootingTestEventAuthorization.ShootingTestEventPermission.VIEW_PARTICIPANTS;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class ShootingTestEventAuthorizationTest extends AbstractShootingTestEntityAuthorizationTest<ShootingTestEvent> {

    private CalendarEvent calendarEvent;

    @Override
    protected void initBeforeCreatingTargetEntity(final Riistanhoitoyhdistys rhy) {
        calendarEvent = model().newCalendarEvent(rhy, some(shootingTestTypes()));
    }

    @Override
    protected ShootingTestEvent createEntity() {
        return model().newShootingTestEvent(calendarEvent);
    }

    @Test
    public void testActiveShootingTestOfficial() {
        withPerson(person -> {
            model().newOccupation(getRhy(), person, AMPUMAKOKEEN_VASTAANOTTAJA);
            onSavedAndAuthenticated(createUser(person), this::assertPermittedAsUnassignedOfficial);
        });
    }

    @Test
    public void testAssignedOfficial() {
        withPerson(person -> {
            model().newShootingTestOfficial(getEntity(), person);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(true));
        });
    }

    @Test
    public void testAssignedOfficial_afterOneWeek() {
        withPerson(person -> {
            calendarEvent.setDate(today().minus(DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL).minusDays(1).toDate());
            model().newShootingTestOfficial(getEntity(), person);

            onSavedAndAuthenticated(createUser(person), this::assertPermittedAsUnassignedOfficial);
        });
    }

    @Override
    protected void testAllPermissions(final boolean permitted, final int maxQueryCount) {
        testPermission(READ, permitted, maxQueryCount);
        testPermission(UPDATE, permitted, maxQueryCount);
        testPermission(ASSIGN_OFFICIALS, permitted, maxQueryCount);
        testPermission(VIEW_PARTICIPANTS, permitted, maxQueryCount);

        // Create a transient event (not yet persisted).
        createEntity();
        testPermission(CREATE, permitted, maxQueryCount);
    }

    private void assertPermittedAsUnassignedOfficial() {
        testPermission(READ, true, 3);
        testPermission(ASSIGN_OFFICIALS, true, 4);
        testPermission(VIEW_PARTICIPANTS, false, 4);
        testPermission(UPDATE, false, 4);

        // Create a transient event (not yet persisted).
        createEntity();
        testPermission(CREATE, true, 4);
    }
}
