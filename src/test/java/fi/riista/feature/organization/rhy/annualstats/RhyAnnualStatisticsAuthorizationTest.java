package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class RhyAnnualStatisticsAuthorizationTest extends EmbeddedDatabaseTest {

    @Resource
    private RhyAnnualStatisticsRepository repository;

    private int year;
    private Riistanhoitoyhdistys rhy;
    private RhyAnnualStatistics statistics;

    @Before
    public void setup() {
        year = today().getYear();
        rhy = model().newRiistanhoitoyhdistys();
        statistics = createAnnualStatistics();
    }

    private RhyAnnualStatistics createAnnualStatistics() {
        return model().newRhyAnnualStatistics(rhy, year--);
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> testAllPermissions(true, 1));
    }

    @Test
    public void testModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> testAllPermissions(true, 1));
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
        withRhyAndCoordinator((anotherRhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testNonCoordinatorOccupations() {
        OccupationType.getApplicableTypes(RHY)
                .stream()
                .filter(occType -> occType != TOIMINNANOHJAAJA)
                .forEach(occupationType -> withPerson(person -> {

                    person.setRhyMembership(rhy);
                    model().newOccupation(rhy, person, occupationType);

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

    private void testAllPermissions(final boolean permitted, final int maxQueryCount) {
        testPermission(READ, permitted, maxQueryCount);
        testPermission(UPDATE, permitted, maxQueryCount);
        testPermission(DELETE, false, maxQueryCount);

        // Create a transient report (not yet persisted).
        createAnnualStatistics();
        testPermission(CREATE, permitted, maxQueryCount);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int maxQueryCount) {
        onCheckingPermission(permission).expect(permitted).expectNumberOfQueriesAtMost(maxQueryCount).apply(statistics);
    }
}
