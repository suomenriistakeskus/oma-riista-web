package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;

import static fi.riista.feature.huntingclub.group.HuntingClubGroupAuthorization.Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static java.util.Arrays.asList;

public class HuntingClubGroupAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void testGroupLeaderPermissions() {
        testGroupMembershipPermissions(OccupationType.RYHMAN_METSASTYKSENJOHTAJA, false, true, true, false, true);
    }

    @Test
    public void testGroupMemberPermissions() {
        testGroupMembershipPermissions(OccupationType.RYHMAN_JASEN, false, true, false, false, false);
    }

    private void testGroupMembershipPermissions(
            final OccupationType occupationType,
            final boolean canCreate,
            final boolean canRead,
            final boolean canUpdate,
            final boolean canDelete,
            final boolean canLinkHarvests) {

        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Person groupMember = model().newHuntingClubGroupMember(group, occupationType).getPerson();

        onSavedAndAuthenticated(createUser(groupMember), tx(() -> {
            assertHasPermission(canCreate, group, CREATE);
            assertHasPermission(canRead, group, READ);
            assertHasPermission(canUpdate, group, UPDATE);
            assertHasPermission(canDelete, group, DELETE);
            assertHasPermission(canLinkHarvests, group, LINK_DIARY_ENTRY_TO_HUNTING_DAY);
        }));
    }

    @Test
    public void testClubContactWithoutGroupMembership() {
        final HuntingClub club = model().newHuntingClub();
        final Person clubContact = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        onSavedAndAuthenticated(createNewUser("yhdyshenkilo", clubContact), tx(() -> {
            assertHasPermissions(group, asList(CREATE, READ, UPDATE, DELETE, LINK_DIARY_ENTRY_TO_HUNTING_DAY));
        }));
    }

    @Test
    public void testAdminPermissions() {
        assertModeratorPermissions(SystemUser.Role.ROLE_ADMIN);
    }

    @Test
    public void testModeratorPermissions() {
        assertModeratorPermissions(SystemUser.Role.ROLE_MODERATOR);
    }

    private void assertModeratorPermissions(final SystemUser.Role role) {
        final HuntingClubGroup group = model().newHuntingClubGroup();

        onSavedAndAuthenticated(createNewUser(role), tx(() -> {
            assertHasPermissions(
                    group, asList(CREATE, READ, UPDATE, DELETE, LINK_DIARY_ENTRY_TO_HUNTING_DAY));
        }));
    }
}
