package fi.riista.feature.permit.application.schedule;

import fi.riista.config.Constants;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_APPLICATION_SCHEDULE;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class HarvestPermitApplicationScheduleAuthorizationTest extends EmbeddedDatabaseTest {

    private HarvestPermitApplicationSchedule schedule;

    @Before
    public void setup() {
        schedule = model().newHarvestPermitApplicationSchedule(HarvestPermitCategory.MOOSELIKE,
                new DateTime(2021, 10, 9, 0, 0, Constants.DEFAULT_TIMEZONE),
                new DateTime(2022, 1, 15, 0, 0, Constants.DEFAULT_TIMEZONE),
                "Ohjeet",
                "Anvisning",
                null);
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            testUpdate(true, 0, schedule);
            testCreateReadDelete(false, 0, schedule);
        });
    }

    @Test
    public void testModerator_withPrivileges() {
        onSavedAndAuthenticated(createNewModerator(MODERATE_APPLICATION_SCHEDULE), () -> {
            testUpdate(true, 0, schedule);
            testCreateReadDelete(false, 0, schedule);
        });
    }

    @Test
    public void testModerator_withoutPrivileges() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            testUpdate(false, 0, schedule);
            testCreateReadDelete(false, 0, schedule);
        });
    }

    @Test
    public void testUser() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            testUpdate(false, 0, schedule);
            testCreateReadDelete(false, 0, schedule);
        });
    }

    private void testUpdate(final boolean permitted, final int queryCount, final HarvestPermitApplicationSchedule schedule) {
        testPermission(UPDATE, permitted, queryCount, schedule);
    }

    private void testCreateReadDelete(final boolean permitted, final int queryCount, final HarvestPermitApplicationSchedule schedule) {
        testPermission(READ, permitted, queryCount, schedule);
        testPermission(DELETE, permitted, queryCount, schedule);

        testCreate(permitted, queryCount);
    }

    private void testCreate(final boolean permitted, final int queryCount) {
        // Create a transient event (not yet persisted).
        final HarvestPermitApplicationSchedule newSchedule = model().newHarvestPermitApplicationSchedule(HarvestPermitCategory.MOOSELIKE,
                new DateTime(2021, 10, 9, 0, 0, Constants.DEFAULT_TIMEZONE),
                new DateTime(2022, 1, 15, 0, 0, Constants.DEFAULT_TIMEZONE),
                "Ohjeet",
                "Anvisning",
                null);

        testPermission(CREATE, permitted, queryCount, newSchedule);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount, final HarvestPermitApplicationSchedule schedule) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(schedule);
    }
}
