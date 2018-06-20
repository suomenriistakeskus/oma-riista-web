package fi.riista.feature.organization;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import static fi.riista.feature.organization.RiistakeskusAuthorization.Permission.LIST_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class RiistakeskusAuthorizationTest extends EmbeddedDatabaseTest {

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
        withRhy(rhy -> withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                testPermission(READ, false, 2);
                testPermission(UPDATE, false, 2);
                testPermission(LIST_ANNUAL_STATISTICS, true, 2);
            });
        }));
    }

    @Test
    public void testExpiredCoordinator() {
        withRhy(rhy -> withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testActiveSrvaContactPerson() {
        withRhy(rhy -> withPerson(person -> {
            model().newOccupation(rhy, person, SRVA_YHTEYSHENKILO);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testActiveShootingTestOfficial() {
        withRhy(rhy -> withPerson(person -> {
            model().newOccupation(rhy, person, AMPUMAKOKEEN_VASTAANOTTAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testNonRhyOccupiedPerson() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> testAllPermissions(false));
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int maxQueries) {
        onCheckingPermission(permission)
                .expect(permitted)
                .expectNumberOfQueriesAtMost(maxQueries)
                .apply(getRiistakeskus());
    }

    private void testAllPermissions(final boolean permitted) {
        testAllPermissions(permitted, 2);
    }

    private void testAllPermissions(final boolean permitted, final int maxQueries) {
        testPermission(READ, permitted, maxQueries);
        testPermission(UPDATE, permitted, maxQueries);
        testPermission(LIST_ANNUAL_STATISTICS, permitted, maxQueries);
    }
}
