package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;

import static fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryAuthorization.Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT;
import static fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryAuthorization.Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED;
import static fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryAuthorization.Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class MooseHuntingSummaryAuthorizationTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    private static final BiFunction<HarvestPermit, HuntingClub, MooseHuntingSummary> CREATE_PERMIT_PARTNER =
            (permit, club) -> new MooseHuntingSummary(club, permit);

    private final BiFunction<HarvestPermit, HuntingClub, MooseHuntingSummary> CREATE_ENTITY =
            (permit, club) -> model().newMooseHuntingSummary(permit, club, true);

    @Test
    public void testRead_forAllowedPermissions_withEntity() {
        buildReadPermittedUsers().forEach(userFn -> {
            assertUserIsPermitted(READ, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testRead_forDeniedPermissions_withEntity() {
        // Test with non-club-related person/user.
        assertUserNotPermitted(READ, (club, group) -> createUserWithPerson(), CREATE_ENTITY);
    }

    @Test
    public void testCreate_forAllowedPermissions() {
        buildWritePermittedUsers().forEach(userFn -> {
            assertUserIsPermitted(CREATE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testCreate_forDeniedPermissions() {
        buildNonWritePermittedUsers().forEach(userFn -> {
            assertUserNotPermitted(CREATE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testCreateMooseDataCardOriginated_forAllowedPermissions() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testCreateMooseDataCardOriginated_forDeniedPermissions() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testCreate_forDeniedPermissions_whenClubNotPartner() {
        // All users (including admin/moderator) should be denied because club is not an actual
        // partner of permit.

        clubGroupUserFunctionsBuilder().withAll().build().forEach(userFn -> {
            testClubThatIsNotPartner(CREATE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdate_forAllowedPermissions_withPermitPartner() {
        buildWritePermittedUsers().forEach(userFn -> {
            assertUserIsPermitted(UPDATE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdate_forAllowedPermissions_withEntity() {
        buildWritePermittedUsers().forEach(userFn -> {
            assertUserIsPermitted(UPDATE, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testUpdate_forDeniedPermissions_withPermitPartner() {
        buildNonWritePermittedUsers().forEach(userFn -> {
            assertUserNotPermitted(UPDATE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdate_forDeniedPermissions_withEntity() {
        buildNonWritePermittedUsers().forEach(userFn -> {
            assertUserNotPermitted(UPDATE, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testUpdateMooseDataCardOriginated_forAllowedPermissions_withPermitPartner() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(UPDATE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdateMooseDataCardOriginated_forAllowedPermissions_withEntity() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(UPDATE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testUpdateMooseDataCardOriginated_forDeniedPermissions_withPermitPartner() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(UPDATE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdateMooseDataCardOriginated_forDeniedPermissions_withEntity() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(UPDATE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testDelete_forAllowedPermissions_withPermitPartner() {
        buildWritePermittedUsers().forEach(userFn -> {
            assertUserIsPermitted(DELETE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testDelete_forAllowedPermissions_withEntity() {
        buildWritePermittedUsers().forEach(userFn -> {
            assertUserIsPermitted(DELETE, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testDelete_forDeniedPermissions_withPermitPartner() {
        buildNonWritePermittedUsers().forEach(userFn -> {
            assertUserNotPermitted(DELETE, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testDelete_forDeniedPermissions_withEntity() {
        buildNonWritePermittedUsers().forEach(userFn -> {
            assertUserNotPermitted(DELETE, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testDeleteMooseDataCardOriginated_forAllowedPermissions_withPermitPartner() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(DELETE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testDeleteMooseDataCardOriginated_forAllowedPermissions_withEntity() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(DELETE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testDeleteMooseDataCardOriginated_forDeniedPermissions_withPermitPartner() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(DELETE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testDeleteMooseDataCardOriginated_forDeniedPermissions_withEntity() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(DELETE_MOOSE_DATA_CARD_ORIGINATED, userFn, CREATE_ENTITY);
            reset();
        });
    }

    private void assertUserIsPermitted(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermit, HuntingClub, MooseHuntingSummary> createAuthorizedObject) {

        test(permission, true, createUser, createAuthorizedObject, false);
    }

    private void assertUserNotPermitted(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermit, HuntingClub, MooseHuntingSummary> createAuthorizedObject) {

        test(permission, false, createUser, createAuthorizedObject, false);
    }

    private void testClubThatIsNotPartner(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermit, HuntingClub, MooseHuntingSummary> createAuthorizedObject) {

        test(permission, false, createUser, createAuthorizedObject, true);
    }

    private void test(final Enum<?> permission,
                      final boolean expected,
                      final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
                      final BiFunction<HarvestPermit, HuntingClub, MooseHuntingSummary> createAuthorizedObject,
                      final boolean detachClubFromPartnership) {

        runInTransaction(() -> withMooseHuntingGroupFixture(f -> {
            if (detachClubFromPartnership) {
                f.permit.getPermitPartners().remove(f.club);
            }

            // Persist before creation of authorized object because it might depend on database IDs!
            persistInCurrentlyOpenTransaction();

            final SystemUser user = createUser.apply(f.club, f.group);
            final MooseHuntingSummary obj = createAuthorizedObject.apply(f.permit, f.club);

            persistInCurrentlyOpenTransaction();
            authenticate(user);

            onCheckingPermission(permission).expect(expected).joinToCurrentTransaction().apply(obj);
        }));
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildReadPermittedUsers() {
        return clubGroupUserFunctionsBuilder()
                .withOriginalPermitContactPerson(true)
                .withPermitContactPerson(true)
                .withAdminAndModerator(true)
                .build();
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildWritePermittedUsers() {
        return clubGroupUserFunctionsBuilder()
                .withClubMember(false)
                .withGroupMember(false)
                .withOriginalPermitContactPerson(true)
                .withPermitContactPerson(true)
                .withAdminAndModerator(true)
                .build();
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildNonWritePermittedUsers() {
        return clubGroupUserFunctionsBuilder()
                .withClubContactPerson(false)
                .withGroupLeader(false)
                .build();
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildAdminAndModerator() {
        return clubGroupUserFunctionsBuilder().createAdminAndModerator();
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildNonModeratorUsers() {
        return clubGroupUserFunctionsBuilder().withAllButAdminAndModerator().build();
    }
}
