package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import fi.riista.util.VersionedTestExecutionSupport;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class HarvestLockedConditionTest implements ValueGeneratorMixin, VersionedTestExecutionSupport<HarvestSpecVersion> {
    private static final Predicate<Harvest> ALWAYS_TRUE = h -> true;
    private static final Predicate<Harvest> ALWAYS_FALSE = h -> false;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(HarvestSpecVersion.class));
    }

    private Person activePerson;
    private Predicate<Harvest> contactPersonTester;

    @Before
    public void init() {
        activePerson = new Person();
        activePerson.setId(nextLong());
        contactPersonTester = HarvestLockedCondition.createContactPersonTester(activePerson);
    }

    private void assertMobileCanEdit(final boolean expectedResult, final Harvest harvest,
                                     final HarvestSpecVersion specVersion) {
        assertEquals(expectedResult, HarvestLockedCondition.canEdit(
                activePerson, harvest, specVersion,
                ALWAYS_FALSE, contactPersonTester));
    }

    private void assertWebCanEdit(final boolean expectedResult, final Harvest harvest) {
        assertEquals(expectedResult, HarvestLockedCondition.canEdit(
                activePerson, harvest, null,
                ALWAYS_FALSE, contactPersonTester));
    }

    private static void assertModeratorCanEdit(final boolean expectedResult, final Harvest harvest) {
        assertEquals(expectedResult, HarvestLockedCondition.canEdit(
                null, harvest, null,
                ALWAYS_FALSE, ALWAYS_FALSE));
    }

    // DIARY

    @Test
    public void testHarvest_Diary() {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(true, harvest, specVersion);
        });

        assertModeratorCanEdit(false, harvest);
    }

    @Test
    public void testHarvest_Diary_Mooselike_AsModerator() {
        final GameSpecies species = new GameSpecies();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setSpecies(species);

        for (int speciesCode : GameSpecies.ALL_GAME_SPECIES_CODES) {
            species.setOfficialCode(speciesCode);

            forEachVersion(specVersion -> {
                if (GameSpecies.isMooseOrDeerRequiringPermitForHunting(speciesCode)) {
                    assertModeratorCanEdit(true, harvest);
                } else {
                    assertModeratorCanEdit(false, harvest);
                }
            });
        }
    }

    // HUNTING DAY

    @Test
    public void testHarvest_WithHuntingDay() {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.updateHuntingDayOfGroup(new GroupHuntingDay(), null);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            assertEquals(false, HarvestLockedCondition.canEdit(
                    activePerson, harvest, specVersion,
                    ALWAYS_TRUE, ALWAYS_FALSE));

            assertEquals(false, HarvestLockedCondition.canEdit(
                    activePerson, harvest, specVersion,
                    ALWAYS_FALSE, ALWAYS_FALSE));
        });

        assertModeratorCanEdit(true, harvest);
    }

    // SEASON

    @Test
    public void testHarvest_WithSeason_ReportSentForApproval() {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestSeason(new HarvestSeason());
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            if (specVersion.supportsHarvestReport()) {
                assertMobileCanEdit(true, harvest, specVersion);
            } else {
                assertMobileCanEdit(false, harvest, specVersion);
            }
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithSeason_ReportApproved() {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestSeason(new HarvestSeason());
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithSeason_ReportRejected() {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestSeason(new HarvestSeason());
        harvest.setHarvestReportState(HarvestReportState.REJECTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    // PERMIT

    // accepted for permit

    @Test
    public void testHarvest_WithPermit_Accepted() {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Accepted_AsContactPerson() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            if (specVersion.supportsHarvestPermitState()) {
                assertMobileCanEdit(true, harvest, specVersion);
            } else {
                assertMobileCanEdit(false, harvest, specVersion);
            }
        });
    }

    // accepted for permit with normal harvest report

    @Test
    public void testHarvest_WithPermit_Accepted_WithReportSentForApproval() {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Accepted_WithReportSentForApproval_AsContactPerson() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            if (specVersion.supportsHarvestPermitState()) {
                assertMobileCanEdit(true, harvest, specVersion);
            } else {
                assertMobileCanEdit(false, harvest, specVersion);
            }
        });
    }

    @Test
    public void testHarvest_WithPermit_Accepted_WithReportApproved() {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Accepted_WithReportApproved_AsContactPerson() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });
    }

    @Test
    public void testHarvest_WithPermit_Accepted_WithReportRejected() {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.REJECTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Accepted_WithReportRejected_AsContactPerson() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.REJECTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });
    }

    // accepted for permit with end of hunting report

    @Test
    public void testHarvest_WithPermit_Accepted_EndOfHuntingSentForApproval() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);
        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Accepted_EndOfHuntingApproved() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);
        permit.setHarvestReportState(HarvestReportState.APPROVED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(false, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Accepted_EndOfHuntingRejected() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);
        permit.setHarvestReportState(HarvestReportState.REJECTED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);

        forEachVersion(specVersion -> {
            assertMobileCanEdit(false, harvest, specVersion);
        });

        assertModeratorCanEdit(false, harvest);
    }

    // rejected for permit

    @Test
    public void testHarvest_WithPermit_Rejected_EndOfHuntingSentForApproval() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            if (specVersion.supportsHarvestPermitState()) {
                assertMobileCanEdit(true, harvest, specVersion);
            } else {
                assertMobileCanEdit(false, harvest, specVersion);
            }
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Rejected_EndOfHuntingAccepted() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setHarvestReportState(HarvestReportState.APPROVED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            if (specVersion.supportsHarvestPermitState()) {
                assertMobileCanEdit(true, harvest, specVersion);
            } else {
                assertMobileCanEdit(false, harvest, specVersion);
            }
        });

        assertModeratorCanEdit(true, harvest);
    }

    @Test
    public void testHarvest_WithPermit_Rejected_EndOfHuntingRejected() {
        final HarvestPermit permit = new HarvestPermit();
        permit.setHarvestReportState(HarvestReportState.REJECTED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);

        assertWebCanEdit(true, harvest);

        forEachVersion(specVersion -> {
            if (specVersion.supportsHarvestPermitState()) {
                assertMobileCanEdit(true, harvest, specVersion);
            } else {
                assertMobileCanEdit(false, harvest, specVersion);
            }
        });

        assertModeratorCanEdit(true, harvest);
    }
}
