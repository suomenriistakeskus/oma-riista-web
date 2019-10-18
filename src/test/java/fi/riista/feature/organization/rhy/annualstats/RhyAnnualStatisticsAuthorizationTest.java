package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_RHY_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsAuthorization.Permission.CHANGE_APPROVAL_STATUS;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsAuthorization.Permission.MODERATOR_UPDATE;
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
        onSavedAndAuthenticated(createNewModerator(), () -> testAllPermissions(true, false, false, 1));
    }

    @Test
    public void testPrivilegedModerator() {
        onSavedAndAuthenticated(createNewModerator(MODERATE_RHY_ANNUAL_STATISTICS), () -> testAllPermissions(true, 1));
    }

    @Test
    public void testActiveCoordinator() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(true, true, false, 2));
        });
    }

    @Test
    public void testExpiredCoordinator() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false, 2));
        });
    }

    @Test
    public void testActiveCoordinatorInDifferentRhy() {
        withRhyAndCoordinator((anotherRhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> testAllPermissions(false, 2));
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

                    onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false, 2));
                }));
    }

    @Test
    public void testNonRhyOccupiedPerson() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> testAllPermissions(false, 2));
    }

    private void testAllPermissions(final boolean allPermitted, final int maxQueryCount) {
        testAllPermissions(allPermitted, allPermitted, allPermitted, maxQueryCount);
    }

    private void testAllPermissions(final boolean createOrRead,
                                    final boolean update,
                                    final boolean updateOrApprovalByModerator,
                                    final int maxQueryCount) {

        testPermission(READ, createOrRead, maxQueryCount);
        testPermission(UPDATE, update, maxQueryCount);
        testPermission(MODERATOR_UPDATE, updateOrApprovalByModerator, maxQueryCount);
        testPermission(CHANGE_APPROVAL_STATUS, updateOrApprovalByModerator, maxQueryCount);
        testPermission(DELETE, false, maxQueryCount);

        // Create a transient report (not yet persisted).
        createAnnualStatistics();
        testPermission(CREATE, createOrRead, maxQueryCount);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int maxQueryCount) {
        onCheckingPermission(permission).expect(permitted).expectNumberOfQueriesAtMost(maxQueryCount).apply(statistics);
    }
}
