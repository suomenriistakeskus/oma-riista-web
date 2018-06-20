package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.shootingtest.ShootingTestEvent.DAYS_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class ShootingTestParticipantAuthorizationTest
        extends AbstractShootingTestEntityAuthorizationTest<ShootingTestParticipant> {

    private ShootingTestEvent event;
    private Person attendeePerson;

    @Override
    protected void initBeforeCreatingTargetEntity(final Riistanhoitoyhdistys rhy) {
        event = model().newShootingTestEvent(rhy);
        attendeePerson = model().newPerson();
    }

    @Override
    protected ShootingTestParticipant createEntity() {
        return model().newShootingTestParticipant(event, attendeePerson);
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
            model().newShootingTestOfficial(event, person);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(true));
        });
    }

    @Test
    public void testAssignedOfficial_afterOneWeek() {
        withPerson(person -> {
            event.getCalendarEvent().setDate(today().minus(DAYS_UPDATEABLE_BY_OFFICIAL).minusDays(1).toDate());
            model().newShootingTestOfficial(event, person);

            onSavedAndAuthenticated(createUser(person), this::assertPermittedAsUnassignedOfficial);
        });
    }

    @Override
    protected void testAllPermissions(final boolean permitted, final int maxQueryCount) {
        testPermission(READ, permitted, maxQueryCount);
        testPermission(UPDATE, permitted, maxQueryCount);

        // Create a transient participant (not yet persisted).
        createEntity();
        testPermission(CREATE, permitted, maxQueryCount);
    }

    private void assertPermittedAsUnassignedOfficial() {
        testPermission(READ, true, 3);
        testPermission(UPDATE, false, 4);

        // Create a transient participant (not yet persisted).
        createEntity();
        testPermission(CREATE, false, 4);
    }
}
