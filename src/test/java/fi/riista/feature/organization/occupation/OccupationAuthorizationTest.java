package fi.riista.feature.organization.occupation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.function.Consumer;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;

public class OccupationAuthorizationTest extends EmbeddedDatabaseTest {

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Override
    public void withRhy(final Consumer<Riistanhoitoyhdistys> consumer) {
        consumer.accept(createRhy());
    }

    @Test
    public void testRhyOccupationsForAdmin() {
        withRhyAndSomeOccupation((rhy, occupation) -> {
            onSavedAndAuthenticated(createNewAdmin(), () -> assertPermissions(occupation, EntityPermission.crud()));
        });
    }

    @Test
    public void testRhyOccupationsForModerator() {
        withRhyAndSomeOccupation((rhy, occupation) -> {
            onSavedAndAuthenticated(createNewModerator(), () -> assertPermissions(occupation, EntityPermission.crud()));
        });
    }

    @Test
    public void testRhyOccupationsForUser() {
        withRhyAndSomeOccupation((rhy, occupation) -> {
            onSavedAndAuthenticated(createNewUser(), () -> assertPermissions(occupation, EntityPermission.none()));
        });
    }

    @Test
    public void testRhyOccupationsForCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Occupation occupation = model().newOccupation(rhy, model().newPerson());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                assertPermissions(occupation, EntityPermission.crud());
            });
        });
    }

    @Test
    public void testRhyOccupationsForOtherRhyCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Occupation occupation = model().newOccupation(createRhy(), model().newPerson());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                assertPermissions(occupation, EntityPermission.none());
            });
        });
    }

    // SEURAN_YHDYSHENKILO

    @Test
    public void testClubOccupationsForClubManager() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(club, SEURAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(club, SEURAN_YHDYSHENKILO), () -> {
            assertPermissions(occupation, EntityPermission.crud());
        });
    }

    @Test
    public void testClubOccupationsForOtherClubManager() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(club, SEURAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(createClub(), SEURAN_YHDYSHENKILO), () -> {
            assertPermissions(occupation, EntityPermission.none());
        });
    }

    @Test
    public void testGroupOccupationsForClubManager() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(createGroup(club), RYHMAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(club, SEURAN_YHDYSHENKILO), () -> {
            assertPermissions(occupation, EntityPermission.crud());
        });
    }

    // RYHMAN_METSASTYKSENJOHTAJA

    @Test
    public void testClubGroupOccupationsForGroupManager() {
        final HuntingClubGroup group = createGroup(createClub());
        final Occupation occupation = createOccupation(group, RYHMAN_JASEN);

        onSavedAndAuthenticated(createUserToRole(group, RYHMAN_METSASTYKSENJOHTAJA), () -> {
            assertPermissions(occupation, EntityPermission.crud());
        });
    }

    @Test
    public void testGroupOccupationsForOtherGroupManager() {
        final Occupation occupation = createOccupation(createGroup(createClub()), RYHMAN_JASEN);

        final HuntingClubGroup group2 = createGroup(createClub());

        onSavedAndAuthenticated(createUserToRole(group2, RYHMAN_METSASTYKSENJOHTAJA), () -> {
            assertPermissions(occupation, EntityPermission.none());
        });
    }

    // SEURAN_JASEN

    @Test
    public void testOwnClubOccupationForClubMember() {
        final Occupation occupation = createOccupation(createClub(), SEURAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", occupation.getPerson()), () -> {
            assertPermissions(occupation, EnumSet.of(CREATE, READ, DELETE));
        });
    }

    @Test
    public void testClubOccupationForOtherClubMember() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(club, SEURAN_JASEN);

        final Occupation jasenOccupation = createOccupation(club, SEURAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", jasenOccupation.getPerson()), () -> {
            assertPermissions(occupation, EntityPermission.none());
        });
    }

    // RYHMAN_JASEN

    @Test
    public void testOwnGroupOccupationForGroupMember() {
        final Occupation occupation = createOccupation(createGroup(createClub()), RYHMAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", occupation.getPerson()), () -> {
            assertPermissions(occupation, EnumSet.of(READ, DELETE));
        });
    }

    @Test
    public void testGroupOccupationForOtherGroupMember() {
        final HuntingClub club = createClub();
        final Occupation occupation = createOccupation(createGroup(club), RYHMAN_JASEN);

        final Occupation jasenOccupation = createOccupation(createGroup(club), RYHMAN_JASEN);

        onSavedAndAuthenticated(createNewUser("jasen", jasenOccupation.getPerson()), () -> {
            assertPermissions(occupation, EntityPermission.none());
        });
    }

    @Test
    public void testPersonCanCreateAndDeleteClubOccupationToHimself() {
        withPerson(person -> {
            final HuntingClub club = createClub();

            onSavedAndAuthenticated(createUser(person), () -> {
                final Occupation occupation = new Occupation();
                occupation.setPerson(person);
                occupation.setOrganisationAndOccupationType(club, SEURAN_JASEN);

                assertPermissions(occupation, EnumSet.of(CREATE, DELETE));
            });
        });
    }

    private void assertPermissions(final Occupation occupation, final EnumSet<EntityPermission> permissions) {
        assertHasPermissions(occupation, permissions);
        assertNoPermissions(occupation, EnumSet.complementOf(permissions));
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
