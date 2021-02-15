package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;


import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;

public class HuntingControlAttachmentAuthorizationTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;
    private HuntingControlEvent event;
    private PersistentFileMetadata metadata;
    private HuntingControlAttachment attachment;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        event = model().newHuntingControlEvent(rhy);
        metadata = model().newPersistentFileMetadata();
        createAttachment();
    }

    private void createAttachment() {
        attachment = model().newHuntingControlAttachment(event, metadata);
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
        testReadUpdateDeletePermissions(permitted, 3);
        testCreatePermission(permitted, 2);
    }

    private void testAllPermissions(final boolean permitted, final int queryCount) {
        testReadUpdateDeletePermissions(permitted, queryCount);
        testCreatePermission(permitted, queryCount);
    }

    private void testCreatePermission(final boolean permitted, final int queryCount) {
        // Create a transient attachment (not yet persisted).
        createAttachment();
        testPermission(CREATE, permitted, queryCount);
    }

    private void testReadUpdateDeletePermissions(final boolean permitted, final int queryCount) {
        testPermission(READ, permitted, queryCount);
        testPermission(UPDATE, permitted, queryCount);
        testPermission(DELETE, permitted, queryCount);
    }

    private void testPermission(final Enum<?> permission, final boolean permitted, final int queryCount) {
        onCheckingPermission(permission).expect(permitted).expectQueryCount(queryCount).apply(attachment);
    }

}
