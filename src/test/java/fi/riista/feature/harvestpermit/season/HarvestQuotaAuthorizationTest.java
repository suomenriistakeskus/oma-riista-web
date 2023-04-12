package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class HarvestQuotaAuthorizationTest extends EmbeddedDatabaseTest {

    private HarvestQuota quota;
    private HarvestSeason season;
    private HarvestArea area;

    @Before
    public void setup() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final LocalDate beginDate = new LocalDate(2021, 12, 23);
        final LocalDate endDate = new LocalDate(2021, 12, 31);
        final LocalDate endOfReportingDate = new LocalDate(2022, 1, 7);
        season = model().newHarvestSeason(species, beginDate, endDate, endOfReportingDate);
        area = model().newHarvestArea(HarvestArea.HarvestAreaType.PORONHOITOALUE, "fi", "sv");
        quota = model().newHarvestQuota(season, area, 1);
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            testPermission(READ, true, 0, quota);
            testCreate(true, 0);
            testPermission(UPDATE, true, 0, quota);
            testPermission(DELETE, false, 0, quota);
        });
    }

    @Test
    public void testModerator() {
        onSavedAndAuthenticated(createNewModerator(SystemUserPrivilege.MODERATE_HARVEST_SEASONS), () -> {
            testPermission(READ, true, 0, quota);
            testCreate(true, 0);
            testPermission(UPDATE, true, 0, quota);
            testPermission(DELETE, false, 0, quota);
        });
    }

    @Test
    public void testModerator_withoutPrivileges() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            testPermission(READ, true, 0, quota);
            testCreate(false, 0);
            testPermission(UPDATE, false, 0, quota);
            testPermission(DELETE, false, 0, quota);
        });
    }

    @Test
    public void testUser() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            testPermission(READ, true, 0, quota);
            testCreate(false, 0);
            testPermission(UPDATE, false, 0, quota);
            testPermission(DELETE, false, 0, quota);
        });
    }

    private void testCreate(final boolean permitted, final int queryCount) {
        // Create a transient event (not yet persisted).
        final HarvestQuota newQuota = model().newHarvestQuota(season, area, 2);

        testPermission(CREATE, permitted, queryCount, newQuota);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount, final HarvestQuota quota) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(quota);
    }
}
