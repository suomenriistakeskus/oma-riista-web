package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.shootingtest.ShootingTest.DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class ShootingTestAttemptAuthorizationTest
        extends AbstractShootingTestEntityAuthorizationTest<ShootingTestAttempt> {

    private ShootingTestEvent event;
    private ShootingTestParticipant participant;

    @Override
    protected void initBeforeCreatingTargetEntity(final Riistanhoitoyhdistys rhy) {
        event = model().newShootingTestEvent(rhy);
        participant = model().newShootingTestParticipant(event);
    }

    @Override
    protected ShootingTestAttempt createEntity() {
        return model().newShootingTestAttempt(participant);
    }

    @Test
    public void testActiveShootingTestOfficial() {
        withPerson(person -> {
            model().newOccupation(getRhy(), person, AMPUMAKOKEEN_VASTAANOTTAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testAssignedOfficial() {
        withPerson(person -> {
            model().newShootingTestOfficial(event, person);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(true));
        });
    }

    @Test
    public void testAssignedOfficial_afterOneWeek() {
        withPerson(person -> {
            event.getCalendarEvent().setDate(today().minus(DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL).minusDays(1).toDate());
            model().newShootingTestOfficial(event, person);

            onSavedAndAuthenticated(createUser(person), this::assertOnlyReadPermitted);
        });
    }

    @Override
    protected void testAllPermissions(final boolean permitted, final int maxQueryCount) {
        testPermission(READ, permitted, maxQueryCount);
        testPermission(UPDATE, permitted, maxQueryCount);
        testPermission(DELETE, permitted, maxQueryCount);

        // Create a transient attempt (not yet persisted).
        createEntity();
        testPermission(CREATE, permitted, maxQueryCount);
    }

    private void assertOnlyReadPermitted() {
        testPermission(READ, true, 4);
        testPermission(UPDATE, false, 4);
        testPermission(DELETE, false, 4);

        // Create a transient attempt (not yet persisted).
        createEntity();
        testPermission(CREATE, false, 4);
    }
}
