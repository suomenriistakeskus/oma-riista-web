package fi.riista.feature.permit.application;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_DISABILITY_PERMIT_APPLICATION;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Permission.AMEND;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Permission.CANCEL;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Permission.LIST_CONFLICTS;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class HarvestPermitApplicationAuthorizationTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication mammalApplication;
    private HarvestPermitApplication disabilityApplication;
    private Person contactPerson;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson(rhy);

        mammalApplication = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MAMMAL);
        mammalApplication.setContactPerson(contactPerson);

        disabilityApplication = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DISABILITY);
        disabilityApplication.setContactPerson(contactPerson);
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            testCrudOperations(true, 0, mammalApplication);
            testModeratorOperations(true, 0, mammalApplication);
        });
    }

    @Test
    public void testAdmin_disabilityApplication() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            testCrudOperations(true, 0, disabilityApplication);
            testModeratorOperations(true, 0, disabilityApplication);
        });
    }

    @Test
    public void testModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            testCrudOperations(true, 1, mammalApplication);
            testModeratorOperations(true, 1, mammalApplication);
        });
    }

    @Test
    public void testModerator_disabilityApplication() {
        onSavedAndAuthenticated(createNewModerator(MODERATE_DISABILITY_PERMIT_APPLICATION), () -> {
            testCrudOperations(true, 1, disabilityApplication);
            testModeratorOperations(true, 1, disabilityApplication);
        });
    }

    @Test
    public void testModerator_disabilityApplicationNoPrivilege() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            testCrudOperations(false, 1, disabilityApplication);
            testModeratorOperations(false, 1, disabilityApplication);
        });
    }

    @Test
    public void testContactPerson() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            testCrudOperations(true, 2, mammalApplication);
            testModeratorOperations(false, 1, mammalApplication);
        });
    }

    @Test
    public void testContactPerson_disabilityApplication() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            testCrudOperations(true, 2, disabilityApplication);
            testModeratorOperations(false, 1, disabilityApplication);
        });
    }

    @Test
    public void testCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                testPermission(READ, true, 3, mammalApplication);
                testPermission(UPDATE, false, 2, mammalApplication);
                testPermission(DELETE, false, 2, mammalApplication);
                testCreate(false, 2, HarvestPermitCategory.MAMMAL, contactPerson);

                testModeratorOperations(false, 1, mammalApplication);
            });
        });
    }

    @Test
    public void testCoordinator_disabilityApplication() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                testCrudOperations(false, 2, disabilityApplication);
                testModeratorOperations(false, 1, disabilityApplication);
            });
        });
    }

    @Test
    public void testPersonOtherThanContactPerson() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> {
            testPermission(READ, false, 3, mammalApplication);
            testPermission(UPDATE, false, 2, mammalApplication);
            testPermission(DELETE, false, 2, mammalApplication);
            testCreate(false, 2, HarvestPermitCategory.MAMMAL, contactPerson);

            testModeratorOperations(false, 1, mammalApplication);
        });
    }

    @Test
    public void testPersonOtherThanContactPerson_disabilityApplication() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> {
            testCrudOperations(false, 2, disabilityApplication);
            testModeratorOperations(false, 1, disabilityApplication);
        });
    }

    private void testCrudOperations(final boolean permitted, final int queryCount, final HarvestPermitApplication application) {
        testPermission(READ, permitted, queryCount, application);
        testPermission(UPDATE, permitted, queryCount, application);
        testPermission(DELETE, permitted, queryCount, application);

        testCreate(permitted, queryCount, application.getHarvestPermitCategory(), contactPerson);
    }

    private void testModeratorOperations(final boolean permitted, final int queryCount, final HarvestPermitApplication application) {
        testPermission(LIST_CONFLICTS, permitted, queryCount, application);
        testPermission(AMEND, permitted, queryCount, application);
        testPermission(CANCEL, permitted, queryCount, application);
    }

    private void testCreate(final boolean permitted, final int queryCount, final HarvestPermitCategory category, final Person person) {
        // Create a transient event (not yet persisted).
        final HarvestPermitApplication application = model().newHarvestPermitApplication(rhy, null, category);
        application.setContactPerson(person);

        testPermission(CREATE, permitted, queryCount, application);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount, final HarvestPermitApplication application) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(application);
    }
}
