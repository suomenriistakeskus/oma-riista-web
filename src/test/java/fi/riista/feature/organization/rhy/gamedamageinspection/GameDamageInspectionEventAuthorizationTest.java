package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.organization.occupation.OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class GameDamageInspectionEventAuthorizationTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;
    private GameDamageInspectionEvent event;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        createEvent();
    }

    private void createEvent() {
        final Person inspector = model().newPerson(rhy);
        model().newOccupation(rhy, inspector, RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA, today(), today());

        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_MOOSE);
        event = model().newGameDamageInspectionEvent(rhy, species);
        event.setExpensesIncluded(true);
        event.setInspector(inspector);
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> testAllPermissions(true, 0));
    }

    @Test
    public void testModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> testAllPermissions(true, 0));
    }

    @Test
    public void testActiveCoordinator() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(true));
        });
    }

    @Test
    public void testExpiredCoordinator() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testActiveCoordinatorInDifferentRhy() {
        withRhy(anotherRhy -> withPerson(person -> {
            model().newOccupation(anotherRhy, person, TOIMINNANOHJAAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testNonRhyOccupiedPerson() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> testAllPermissions(false));
    }

    private void testAllPermissions(final boolean permitted) {
        testAllPermissions(permitted, 2);
    }

    private void testAllPermissions(final boolean permitted, final int queryCount) {
        testPermission(READ, permitted, queryCount);
        testPermission(UPDATE, permitted, queryCount);
        testPermission(DELETE, permitted, queryCount);

        // Create a transient event (not yet persisted).
        createEvent();
        testPermission(CREATE, permitted, queryCount);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(event);
    }
}
