package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;

import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class BasicClubHuntingSummaryAuthorizationTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    private static final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, BasicClubHuntingSummary> CREATE_PERMIT_PARTNER =
            (speciesAmount, club) -> new BasicClubHuntingSummary(club, speciesAmount);

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

    private void assertUserIsPermitted(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, BasicClubHuntingSummary> createAuthorizedObject) {

        test(permission, true, createUser, createAuthorizedObject, false);
    }

    private void assertUserNotPermitted(
            final Enum<?> permission,
            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
            final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, BasicClubHuntingSummary> createAuthorizedObject) {

        test(permission, false, createUser, createAuthorizedObject, false);
    }

    private void test(final Enum<?> permission,
                      final boolean expected,
                      final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createUser,
                      final BiFunction<HarvestPermitSpeciesAmount, HuntingClub, BasicClubHuntingSummary> createAuthorizedObject,
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
            final BasicClubHuntingSummary obj = createAuthorizedObject.apply(f.speciesAmount, f.club);

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
}
