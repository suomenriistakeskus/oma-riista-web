package fi.riista.feature.account.user;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.jpa.HibernateStatisticsAssertions;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserAuthorizationHelperTest extends EmbeddedDatabaseTest {

    @Resource
    private UserAuthorizationHelper helper;

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasAnyOfRolesInOrganisation_withApplicableAndNonApplicableOccupationTypes() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

            onSavedAndAuthenticated(
                    createUser(person), () -> runInTransaction(() ->
                            assertTrue(helper.hasAnyOfRolesInOrganisation(club, person,
                                    EnumSet.of(OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_JASEN)))));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisation_shortcircuitingForRhy() {
        withRhyAndCoordinator((rhy, coordinator) -> onSavedAndAuthenticated(
                createUser(coordinator), () -> runInTransaction(() ->
                        assertFalse(helper.hasAnyOfRolesInOrganisation(rhy, coordinator,
                                OccupationType.clubValues())))));
    }

    // Adds more test coverage to previous method with different kind of parameters.
    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisation_shortcircuitingForClub() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

            onSavedAndAuthenticated(createUser(person), () -> runInTransaction(() ->
                    assertFalse(helper.hasAnyOfRolesInOrganisation(
                            club, person, EnumSet.of(OccupationType.TOIMINNANOHJAAJA)))));
        });
    }

    @HibernateStatisticsAssertions(queryCount = 1)
    @Test
    public void testHasAnyOfRolesInOrganisations_withApplicableAndNonApplicableOccupationTypes() {
        withPerson(person -> withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club2, person, OccupationType.SEURAN_JASEN);

            onSavedAndAuthenticated(createUser(person), () -> runInTransaction(() -> {
                final EnumSet<OccupationType> occTypes =
                        EnumSet.of(OccupationType.SEURAN_JASEN, OccupationType.RYHMAN_JASEN);

                assertTrue(helper.hasAnyOfRolesInOrganisations(asList(club, club2), person, occTypes));
            }));
        }));
    }

    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisations_shortcircuitingForRhy() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> runInTransaction(() ->
                    assertFalse(helper.hasAnyOfRolesInOrganisations(singleton(rhy), coordinator,
                            OccupationType.clubValues()))));
        });
    }

    // Adds more test coverage to previous method with different kind of parameters.
    @HibernateStatisticsAssertions(queryCount = 0)
    @Test
    public void testHasAnyOfRolesInOrganisations_shortcircuitingForClub() {
        withPerson(person -> withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
            model().newOccupation(club2, person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> runInTransaction(() ->
                    assertFalse(helper.hasAnyOfRolesInOrganisations(
                            asList(club, club2), person,
                            EnumSet.of(OccupationType.TOIMINNANOHJAAJA)))));
        }));
    }

}
