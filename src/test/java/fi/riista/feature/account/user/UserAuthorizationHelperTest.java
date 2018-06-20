package fi.riista.feature.account.user;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.rules.HibernateStatisticsAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.EnumSet;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.occupation.OccupationType.clubValues;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserAuthorizationHelperTest extends EmbeddedDatabaseTest {

    @Resource
    private UserAuthorizationHelper helper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasRoleInOrganisation() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            persistInNewTransaction();
            runInTransaction(() -> assertTrue(helper.hasRoleInOrganisation(rhy, coordinator, TOIMINNANOHJAAJA)));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasRoleInOrganisation_whenExpectingFalseResult() {
        withPerson(person -> {

            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_JASEN);

            final HuntingClub club2 = model().newHuntingClub();

            persistInNewTransaction();
            runInTransaction(() -> assertFalse(helper.hasRoleInOrganisation(club2, person, SEURAN_JASEN)));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasRoleInOrganisation_shortcircuitingForMismatchingTypes() {
        withPerson(person -> {

            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_JASEN);

            persistInNewTransaction();
            runInTransaction(() -> assertFalse(helper.hasRoleInOrganisation(club, person, TOIMINNANOHJAAJA)));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasAnyOfRolesInOrganisation_withApplicableAndNonApplicableOccupationTypes() {
        withPerson(person -> {

            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_JASEN);

            persistInNewTransaction();

            runInTransaction(() -> {
                assertTrue(helper.hasAnyOfRolesInOrganisation(club, person, EnumSet.of(SEURAN_JASEN, RYHMAN_JASEN)));
            });
        });
    }

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasAnyOfRolesInOrganisation_whenExpectingFalseResult() {
        withPerson(person -> withRhy(rhy -> {

            model().newOccupation(model().newHuntingClub(rhy), person, SEURAN_YHDYSHENKILO);
            model().newOccupation(model().newHuntingClub(rhy), person, SEURAN_JASEN);

            final HuntingClub clubOfInterest = model().newHuntingClub(rhy);

            persistInNewTransaction();

            runInTransaction(() -> {
                final EnumSet<OccupationType> occTypes = EnumSet.of(SEURAN_JASEN, SEURAN_YHDYSHENKILO);

                assertFalse(helper.hasAnyOfRolesInOrganisation(clubOfInterest, person, occTypes));
            });
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisation_shortcircuitingForMismatchingTypes() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            persistInNewTransaction();
            runInTransaction(() -> assertFalse(helper.hasAnyOfRolesInOrganisation(rhy, coordinator, clubValues())));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasAnyOfRolesInOrganisations_withApplicableAndNonApplicableOccupationTypes() {
        withPerson(person -> withRhy(rhy -> {

            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club2, person, SEURAN_JASEN);

            persistInNewTransaction();

            runInTransaction(() -> {
                final EnumSet<OccupationType> occTypes = EnumSet.of(SEURAN_JASEN, RYHMAN_JASEN);

                assertTrue(helper.hasAnyOfRolesInOrganisations(asList(club, club2), person, occTypes));
            });
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasAnyOfRolesInOrganisations_whenExpectingFalseResult() {
        withPerson(person -> withRhy(rhy -> {

            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club2, person, SEURAN_JASEN);

            model().newOccupation(model().newHuntingClub(rhy), person, SEURAN_YHDYSHENKILO);

            persistInNewTransaction();

            runInTransaction(() -> {
                final EnumSet<OccupationType> occTypes = EnumSet.of(SEURAN_YHDYSHENKILO);

                assertFalse(helper.hasAnyOfRolesInOrganisations(asList(club, club2), person, occTypes));
            });
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisations_shortcircuitingForMismatchingTypes1() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            persistInNewTransaction();

            runInTransaction(() -> {
                assertFalse(helper.hasAnyOfRolesInOrganisations(singleton(rhy), coordinator, clubValues()));
            });
        });
    }

    // Adds more test coverage to previous method with different set of parameters.
    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisations_shortcircuitingForMismatchingTypes2() {
        withRhy(rhy -> withPerson(person -> {

            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club, person, SEURAN_JASEN);
            model().newOccupation(club2, person, SEURAN_YHDYSHENKILO);

            persistInNewTransaction();

            runInTransaction(() -> assertFalse(helper.hasAnyOfRolesInOrganisations(
                    asList(club, club2), person, EnumSet.of(TOIMINNANOHJAAJA))));
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testAssertCoordinatorAnywhereOrModerator_asAdmin() {
        testAssertCoordinatorAnywhereOrModerator(createNewAdmin(), true);
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testAssertCoordinatorAnywhereOrModerator_asModerator() {
        testAssertCoordinatorAnywhereOrModerator(createNewModerator(), true);
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorAnywhereOrModerator_asCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            testAssertCoordinatorAnywhereOrModerator(createUser(coordinator), true);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorAnywhereOrModerator_asShootingTestOfficial() {
        withRhyAndShootingTestOfficial((rhy, official) -> {
            testAssertCoordinatorAnywhereOrModerator(createUser(official), false);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorAnywhereOrModerator_asNormalUser() {
        testAssertCoordinatorAnywhereOrModerator(createUser(model().newPerson()), false);
    }

    private void testAssertCoordinatorAnywhereOrModerator(final SystemUser user, final boolean shouldSucceed) {
        onSavedAndAuthenticated(user, () -> {

            if (!shouldSucceed) {
                thrown.expect(AccessDeniedException.class);
                thrown.expectMessage(format("User id:%s is not coordinator anywhere", user.getId()));
            }

            runInTransaction(helper::assertCoordinatorAnywhereOrModerator);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testAssertCoordinatorOrModerator_asAdmin() {
        testAssertCoordinatorOrModerator(createNewAdmin(), model().newRiistanhoitoyhdistys(), true);
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testAssertCoordinatorOrModerator_asModerator() {
        testAssertCoordinatorOrModerator(createNewModerator(), model().newRiistanhoitoyhdistys(), true);
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorOrModerator_asCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            testAssertCoordinatorOrModerator(createUser(coordinator), rhy, true);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorOrModerator_asCoordinatorInDifferentRhy() {
        withRhy(rhy1 -> withRhyAndCoordinator((rhy2, coordinator) -> {
            testAssertCoordinatorOrModerator(createUser(coordinator), rhy1, false);
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorOrModerator_asShootingTestOfficial() {
        withRhyAndShootingTestOfficial((rhy, official) -> {
            testAssertCoordinatorOrModerator(createUser(official), rhy, false);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertCoordinatorOrModerator_asNormalUser() {
        withRhy(rhy -> withPerson(person -> testAssertCoordinatorOrModerator(createUser(person), rhy, false)));
    }

    private void testAssertCoordinatorOrModerator(final SystemUser user,
                                                  final Riistanhoitoyhdistys rhy,
                                                  final boolean shouldSucceed) {

        onSavedAndAuthenticated(user, () -> {

            if (!shouldSucceed) {
                thrown.expect(AccessDeniedException.class);
                thrown.expectMessage(format("User id:%s is not coordinator for rhyId:%s", user.getId(), rhy.getId()));
            }

            runInTransaction(() -> helper.assertCoordinatorOrModerator(rhy.getId()));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testAssertClubContactOrModerator_asAdmin() {
        withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            testAssertClubContactOrModerator(createNewAdmin(), club, true);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testAssertClubContactOrModerator_asModerator() {
        withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            testAssertClubContactOrModerator(createNewModerator(), club, true);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertClubContactOrModerator_asCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            testAssertClubContactOrModerator(createUser(coordinator), club, false);
        });
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertClubContactOrModerator_asClubContact() {
        withRhy(rhy -> withPerson(person -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);
            testAssertClubContactOrModerator(createUser(person), club, true);
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertClubContactOrModerator_asClubContactInDifferentClub() {
        withRhy(rhy -> withPerson(person -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            model().newOccupation(model().newHuntingClub(rhy), person, SEURAN_YHDYSHENKILO);
            testAssertClubContactOrModerator(createUser(person), club, false);
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertClubContactOrModerator_asGroupLeaderOfClub() {
        withRhy(rhy -> withPerson(person -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            model().newOccupation(model().newHuntingClubGroup(club), person, RYHMAN_METSASTYKSENJOHTAJA);
            testAssertClubContactOrModerator(createUser(person), club, false);
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 2)
    @Test
    public void testAssertClubContactOrModerator_asNormalUser() {
        withRhy(rhy -> withPerson(person -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            testAssertClubContactOrModerator(createUser(person), club, false);
        }));
    }

    private void testAssertClubContactOrModerator(final SystemUser user,
                                                  final HuntingClub club,
                                                  final boolean shouldSucceed) {

        onSavedAndAuthenticated(user, () -> {

            if (!shouldSucceed) {
                thrown.expect(AccessDeniedException.class);
                thrown.expectMessage(format("User id:%s is not contact for clubId:%s", user.getId(), club.getId()));
            }

            runInTransaction(() -> helper.assertClubContactOrModerator(club));
        });
    }
}
