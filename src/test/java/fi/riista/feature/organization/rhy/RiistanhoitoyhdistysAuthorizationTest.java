package fi.riista.feature.organization.rhy;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyModeratorPermission.CREATE_NOMINATION_DECISION;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_GAME_DAMAGE_INSPECTION_EVENTS;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_SRVA;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.VIEW_SHOOTING_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.VIEW_SHOOTING_TEST_EVENTS_BY_YEAR;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class RiistanhoitoyhdistysAuthorizationTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        rhy = model().newRiistanhoitoyhdistys();
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
            onSavedAndAuthenticated(createUser(person), () -> {
                testAllCoordinatorPermissions(true);
                testPermission(CREATE_NOMINATION_DECISION, false);
            });
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
    public void testActiveSrvaContactPerson() {
        withPerson(person -> {
            model().newOccupation(rhy, person, SRVA_YHTEYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                testPermission(LIST_SRVA, true);
                testPermission(READ, false);
                testPermission(UPDATE, false);
                testPermission(VIEW_SHOOTING_TEST_EVENTS, false);
                testPermission(VIEW_SHOOTING_TEST_EVENTS_BY_YEAR, false);
                testPermission(LIST_HUNTING_CONTROL_EVENTS, false);
                testPermission(LIST_GAME_DAMAGE_INSPECTION_EVENTS, false);
                testPermission(CREATE_NOMINATION_DECISION, false);
            });
        });
    }

    @Test
    public void testExpiredSrvaContactPerson() {
        withPerson(person -> {
            model().newOccupation(rhy, person, SRVA_YHTEYSHENKILO).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testActiveSrvaContactPersonInDifferentRhy() {
        withRhy(anotherRhy -> withPerson(person -> {
            model().newOccupation(anotherRhy, person, SRVA_YHTEYSHENKILO);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testActiveShootingTestOfficial() {
        withPerson(person -> {
            model().newOccupation(rhy, person, AMPUMAKOKEEN_VASTAANOTTAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                testPermission(VIEW_SHOOTING_TEST_EVENTS, true);
                testPermission(VIEW_SHOOTING_TEST_EVENTS_BY_YEAR, false);
                testPermission(READ, false);
                testPermission(UPDATE, false);
                testPermission(LIST_SRVA, false);
                testPermission(LIST_HUNTING_CONTROL_EVENTS, false);
                testPermission(LIST_GAME_DAMAGE_INSPECTION_EVENTS, false);
                testPermission(CREATE_NOMINATION_DECISION, false);
            });
        });
    }

    @Test
    public void testExpiredShootingTestOfficial() {
        withPerson(person -> {
            model().newOccupation(rhy, person, AMPUMAKOKEEN_VASTAANOTTAJA).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testActiveShootingTestOfficialInDifferentRhy() {
        withRhy(anotherRhy -> withPerson(person -> {
            model().newOccupation(anotherRhy, person, AMPUMAKOKEEN_VASTAANOTTAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testNonRhyOccupiedPerson() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> testAllPermissions(false));
    }

    private void testPermission(final Enum<?> permission, final boolean permitted) {
        testPermission(permission, permitted, 2);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(rhy);
    }

    private void testAllCoordinatorPermissions(final boolean permitted) {
        testAllCoordinatorPermissions(permitted, 2);
    }

    private void testAllCoordinatorPermissions(final boolean permitted, final int queryCount) {
        testPermission(READ, permitted, queryCount);
        testPermission(UPDATE, permitted, queryCount);
        testPermission(LIST_SRVA, permitted, queryCount);
        testPermission(VIEW_SHOOTING_TEST_EVENTS, permitted, queryCount);
        testPermission(VIEW_SHOOTING_TEST_EVENTS_BY_YEAR, permitted, queryCount);
        testPermission(LIST_HUNTING_CONTROL_EVENTS, permitted, queryCount);
        testPermission(LIST_GAME_DAMAGE_INSPECTION_EVENTS, permitted, queryCount);
    }

    private void testAllPermissions(final boolean permitted) {
        testAllPermissions(permitted, 2);
    }

    private void testAllPermissions(final boolean permitted, final int queryCount) {
        testAllCoordinatorPermissions(permitted, queryCount);
        testPermission(CREATE_NOMINATION_DECISION, permitted, queryCount);
    }
}
