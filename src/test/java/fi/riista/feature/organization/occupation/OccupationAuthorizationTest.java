package fi.riista.feature.organization.occupation;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;

public class OccupationAuthorizationTest extends EmbeddedDatabaseTest {
    private static final EnumSet<EntityPermission> ALL_PERMISSIONS = EnumSet.of(
            EntityPermission.CREATE,
            EntityPermission.READ,
            EntityPermission.UPDATE,
            EntityPermission.DELETE);

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    public void testRhyOccupationsForAdmin() {
        final Occupation occupation = createRhyAndOccupation();

        onSavedAndAuthenticated(createNewAdmin(), () -> assertPermissions(occupation, ALL_PERMISSIONS));
    }

    @Test
    public void testRhyOccupationsForModerator() {
        final Occupation occupation = createRhyAndOccupation();

        onSavedAndAuthenticated(createNewModerator(), () -> assertPermissions(occupation, ALL_PERMISSIONS));
    }

    @Test
    public void testRhyOccupationsForUser() {
        final Occupation occupation = createRhyAndOccupation();

        onSavedAndAuthenticated(createNewUser(), () -> {
            assertPermissions(occupation, EnumSet.noneOf(EntityPermission.class));
        });
    }

    @Test
    public void testRhyOccupationsForCoordinator() {
        final Occupation occupation = createRhyAndOccupation();

        onSavedAndAuthenticated(createCoordinator(occupation.getOrganisation()), () -> {
            assertPermissions(occupation, ALL_PERMISSIONS);
        });
    }

    @Test
    public void testRhyOccupationsForOtherRhyCoordinator() {
        final Occupation occupation = createRhyAndOccupation();

        onSavedAndAuthenticated(createCoordinator(createRhy()), () -> {
            assertPermissions(occupation, EnumSet.noneOf(EntityPermission.class));
        });
    }

    private Occupation createRhyAndOccupation() {
        return model().newOccupation(createRhy(), model().newPerson(), OccupationType.METSASTYKSENVALVOJA);
    }

    private SystemUser createCoordinator(final Organisation rhy) {
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        return createNewUser("coordinator", person);
    }

    // SEURAN_YHDYSHENKILO

    @Test
    public void testClubOccupationsForClubManager() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(club, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(club, OccupationType.SEURAN_YHDYSHENKILO), () -> {
            assertPermissions(occupation, ALL_PERMISSIONS);
        });
    }

    @Test
    public void testClubOccupationsForOtherClubManager() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(club, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(createClub(), OccupationType.SEURAN_YHDYSHENKILO), () -> {
            assertPermissions(occupation, EnumSet.noneOf(EntityPermission.class));
        });
    }

    @Test
    public void testGroupOccupationsForClubManager() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(createGroup(club), OccupationType.RYHMAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(club, OccupationType.SEURAN_YHDYSHENKILO), () -> {
            assertPermissions(occupation, ALL_PERMISSIONS);
        });
    }

    // RYHMAN_METSASTYKSENJOHTAJA

    @Test
    public void testClubGroupOccupationsForGroupManager() {
        final HuntingClubGroup group = createGroup(createClub());
        final Occupation occupation = createOccupation(group, OccupationType.RYHMAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA), () -> {
            assertPermissions(occupation, ALL_PERMISSIONS);
        });
    }

    @Test
    public void testGroupOccupationsForOtherGroupManager() {
        final Occupation occupation = createOccupation(createGroup(createClub()), OccupationType.RYHMAN_JASEN);

        final HuntingClubGroup group2 = createGroup(createClub());

        onSavedAndAuthenticated(createUserToRole(group2, OccupationType.RYHMAN_METSASTYKSENJOHTAJA), () -> {
            assertPermissions(occupation, EnumSet.noneOf(EntityPermission.class));
        });
    }

    // SEURAN_JASEN

    @Test
    public void testOwnClubOccupationForClubMember() {
        final Occupation occupation = createOccupation(createClub(), OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", occupation.getPerson()), () -> {
            assertPermissions(occupation, EnumSet.of(EntityPermission.CREATE, EntityPermission.READ, EntityPermission.DELETE));
        });
    }

    @Test
    public void testClubOccupationForOtherClubMember() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(club, OccupationType.SEURAN_JASEN);

        final Occupation jasenOccupation = createOccupation(club, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", jasenOccupation.getPerson()), () -> {
            assertPermissions(occupation, EnumSet.noneOf(EntityPermission.class));
        });
    }

    // RYHMAN_JASEN

    @Test
    public void testOwnGroupOccupationForGroupMember() {
        final Occupation occupation = createOccupation(createGroup(createClub()), OccupationType.RYHMAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", occupation.getPerson()), () -> {
            assertPermissions(occupation, EnumSet.of(EntityPermission.READ, EntityPermission.DELETE));
        });
    }

    @Test
    public void testGroupOccupationForOtherGroupMember() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(createGroup(club), OccupationType.RYHMAN_JASEN);

        final Occupation jasenOccupation = createOccupation(createGroup(club), OccupationType.RYHMAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", jasenOccupation.getPerson()), () -> {
            assertPermissions(occupation, EnumSet.noneOf(EntityPermission.class));
        });
    }

    @Test
    public void testPersonCanCreateAndDeleteClubOccupationToHimself() {
        withPerson(person -> {
            final HuntingClub club = createClub();

            onSavedAndAuthenticated(createUser(person), () -> {
                final OccupationDTO dto = new OccupationDTO();
                dto.setPersonId(person.getId());
                dto.setOrganisationId(club.getId());

                assertPermissions(dto, EnumSet.of(EntityPermission.CREATE, EntityPermission.DELETE));
            });
        });
    }

    private void assertPermissions(final Occupation occupation, final EnumSet<EntityPermission> permissions) {
        final OccupationDTO occupationDTO = OccupationDTO.createWithPerson(occupation);

        runInTransaction(() -> {
            assertHasPermissions(occupation, permissions);
            assertHasPermissions(occupationDTO, permissions);
            assertNoPermissions(occupation, EnumSet.complementOf(permissions));
            assertNoPermissions(occupationDTO, EnumSet.complementOf(permissions));
        });
    }

    private void assertPermissions(final OccupationDTO object, final EnumSet<EntityPermission> permissions) {
        runInTransaction(() -> {
            assertHasPermissions(object, permissions);
            assertNoPermissions(object, EnumSet.complementOf(permissions));
        });
    }

    private SystemUser createUserToRole(final Organisation organisation, final OccupationType occupationType) {
        final Occupation occupation = createOccupation(organisation, occupationType);
        return createUser(occupation.getPerson());
    }

    private Occupation createOccupation(final Organisation org, final OccupationType occupationType) {
        return model().newOccupation(org, model().newPerson(), occupationType);
    }

    private HuntingClub createClub() {
        return model().newHuntingClub(createRhy(), "seura1", "seura1");
    }

    private HuntingClubGroup createGroup(final HuntingClub club) {
        return model().newHuntingClubGroup(club, "ryhma", "ryhma", model().newGameSpecies(), 2014);
    }

    private Riistanhoitoyhdistys createRhy() {
        return model().newRiistanhoitoyhdistys(this.rka);
    }

}
