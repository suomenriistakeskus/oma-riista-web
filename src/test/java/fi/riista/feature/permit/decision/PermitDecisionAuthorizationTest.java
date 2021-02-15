package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_DISABILITY_PERMIT_APPLICATION;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class PermitDecisionAuthorizationTest extends EmbeddedDatabaseTest {

    private PermitDecision mammalDecision;
    private PermitDecision disabilityDecision;

    @Before
    public void setup() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final HarvestPermitApplication mammalApplication = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MAMMAL);
        mammalDecision = model().newPermitDecision(mammalApplication);

        final HarvestPermitApplication disabilityApplication = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DISABILITY);
        disabilityDecision = model().newPermitDecision(disabilityApplication);
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            assertPermissions(true, mammalDecision);
        });
    }

    @Test
    public void testAdmin_disabilityDecision() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            assertPermissions(true, disabilityDecision);
        });
    }

    @Test
    public void testModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            assertPermissions(true, mammalDecision);
        });
    }

    @Test
    public void testModerator_disabilityDecision() {
        onSavedAndAuthenticated(createNewModerator(MODERATE_DISABILITY_PERMIT_APPLICATION), () -> {
            assertPermissions(true, disabilityDecision);
        });
    }

    @Test
    public void testModerator_disabilityDecisionNoPrivilege() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            assertPermissions(false, disabilityDecision);
        });
    }

    @Test
    public void testUser() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            assertPermissions(false, mammalDecision);
        });
    }

    @Test
    public void testUser_disabilityDecision() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            assertPermissions(false, disabilityDecision);
        });
    }

    private void assertPermissions(final boolean permitted, final PermitDecision decision) {
        testPermission(CREATE, permitted, 0, decision);
        testPermission(READ, permitted, 0, decision);
        testPermission(UPDATE, permitted, 0, decision);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount, final PermitDecision decision) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(decision);
    }
}
