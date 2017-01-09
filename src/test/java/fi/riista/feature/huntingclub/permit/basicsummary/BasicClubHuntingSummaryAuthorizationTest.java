package fi.riista.feature.huntingclub.permit.basicsummary;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Authorizable;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.permit.partner.DeerHuntingPermitPartner;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.junit.Test;
import java.util.List;
import java.util.function.BiFunction;

import static fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryAuthorization.Permission.CREATE_MODERATOR_OVERRIDDEN_SUMMARY;
import static fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryAuthorization.Permission.DELETE_MODERATOR_OVERRIDDEN_SUMMARY;
import static fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryAuthorization.Permission.UPDATE_MODERATOR_OVERRIDDEN_SUMMARY;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class BasicClubHuntingSummaryAuthorizationTest extends EmbeddedDatabaseTest {

    private static final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, DeerHuntingPermitPartner> CREATE_PERMIT_PARTNER =
            (speciesAmount, club) -> new DeerHuntingPermitPartner(speciesAmount.getHarvestPermit(), club);

    private final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, BasicClubHuntingSummary> CREATE_ENTITY =
            (speciesAmount, club) -> model().newBasicHuntingSummary(speciesAmount, club, true);

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
    public void testCreateModeratorOverridden_forAllowedPermissions() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(CREATE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testCreateModeratorOverridden_forDeniedPermissions() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(CREATE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_PERMIT_PARTNER);
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
    public void testUpdateModeratorOverridden_forAllowedPermissions_withPermitPartner() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(UPDATE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdateModeratorOverridden_forAllowedPermissions_withEntity() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(UPDATE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testUpdateModeratorOverridden_forDeniedPermissions_withPermitPartner() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(UPDATE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testUpdateModeratorOverridden_forDeniedPermissions_withEntity() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(UPDATE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_ENTITY);
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
    public void testDeleteModeratorOverridden_forAllowedPermissions_withPermitPartner() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(DELETE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testDeleteModeratorOverridden_forAllowedPermissions_withEntity() {
        buildAdminAndModerator().forEach(userFn -> {
            assertUserIsPermitted(DELETE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_ENTITY);
            reset();
        });
    }

    @Test
    public void testDeleteModeratorOverridden_forDeniedPermissions_withPermitPartner() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(DELETE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_PERMIT_PARTNER);
            reset();
        });
    }

    @Test
    public void testDeleteModeratorOverridden_forDeniedPermissions_withEntity() {
        buildNonModeratorUsers().forEach(userFn -> {
            assertUserNotPermitted(DELETE_MODERATOR_OVERRIDDEN_SUMMARY, userFn, CREATE_ENTITY);
            reset();
        });
    }

    private void assertUserIsPermitted(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, ? extends Authorizable> createAuthorizedObject) {

        test(permission, true, createUser, createAuthorizedObject, false);
    }

    private void assertUserNotPermitted(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, ? extends Authorizable> createAuthorizedObject) {

        test(permission, false, createUser, createAuthorizedObject, false);
    }

    private void testClubThatIsNotPartner(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, ? extends Authorizable> createAuthorizedObject) {

        test(permission, false, createUser, createAuthorizedObject, true);
    }

    private void test(
            final Enum<?> permission,
            final boolean expected,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, ? extends Authorizable> createAuthorizedObject,
            final boolean detachClubFromPartnership) {

        runInTransaction(() -> withMooseHuntingGroupFixture(f -> {
            // It is essential to set this to other than the moose code.
            f.species.setOfficialCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);

            if (detachClubFromPartnership) {
                f.permit.getPermitPartners().remove(f.club);
            }

            // Persist before creation of authorized object because it might depend on database IDs!
            persistInCurrentlyOpenTransaction();

            final SystemUser user = createUser.apply(f.club, f.group);
            final Authorizable obj = createAuthorizedObject.apply(f.speciesAmount, f.club);

            persistInCurrentlyOpenTransaction();
            authenticate(user);
            assertHasPermission(expected, obj, permission);
        }));
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildReadPermittedUsers() {
        return clubGroupUserFunctionsBuilder()
                .withOriginalPermitConcatPerson(true)
                .withPermitConcatPerson(true)
                .withAdminAndModerator(true)
                .build();
    }

    private List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> buildWritePermittedUsers() {
        return clubGroupUserFunctionsBuilder()
                .withClubMember(false)
                .withGroupMember(false)
                .withOriginalPermitConcatPerson(true)
                .withPermitConcatPerson(true)
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
