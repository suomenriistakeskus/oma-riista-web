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
import org.junit.Before;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.function.Predicate;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Theories.class)
public class HarvestLockedConditionTest implements ValueGeneratorMixin {

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    private Person activePerson;
    private Predicate<Harvest> contactPersonTester;

    @Before
    public void init() {
        activePerson = new Person();
        activePerson.setId(nextLong());
        contactPersonTester = HarvestLockedCondition.createContactPersonTester(activePerson);
    }

    private void assertMobileCanEdit(final boolean expectedResult,
                                     final Harvest harvest,
                                     final HarvestSpecVersion specVersion) {

        assertEquals(expectedResult, HarvestLockedCondition
                .canEditFromMobile(activePerson, harvest, specVersion, alwaysFalse(), contactPersonTester));
    }

    private void assertWebCanEdit(final boolean expectedResult, final Harvest harvest) {
        assertEquals(expectedResult, HarvestLockedCondition
                .canEditFromWeb(activePerson, harvest, alwaysFalse(), contactPersonTester));
    }

    private static void assertModeratorCanEdit(final boolean expectedResult, final Harvest harvest) {
        assertEquals(expectedResult, HarvestLockedCondition
                .canEditFromWeb(null, harvest, alwaysFalse(), alwaysFalse()));
    }

    // DIARY

    @Theory
    public void testHarvest_diary(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(true, harvest, specVersion);
        assertModeratorCanEdit(false, harvest);
    }

    @Theory
    public void testHarvest_diary_mooselike_asModerator(final HarvestSpecVersion specVersion) {
        final GameSpecies species = new GameSpecies();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setSpecies(species);

        for (final int speciesCode : GameSpecies.ALL_GAME_SPECIES_CODES) {
            species.setOfficialCode(speciesCode);

            if (GameSpecies.isMooseOrDeerRequiringPermitForHunting(speciesCode)) {
                assertModeratorCanEdit(true, harvest);
            } else {
                assertModeratorCanEdit(false, harvest);
            }
        }
    }

    // HUNTING DAY

    @Theory
    public void testHarvest_withHuntingDay(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.updateHuntingDayOfGroup(new GroupHuntingDay(), null);

        assertWebCanEdit(true, harvest);

        assertFalse(HarvestLockedCondition
                .canEditFromMobile(activePerson, harvest, specVersion, alwaysTrue(), alwaysFalse()));

        assertFalse(HarvestLockedCondition
                .canEditFromMobile(activePerson, harvest, specVersion, alwaysFalse(), alwaysFalse()));

        assertModeratorCanEdit(true, harvest);
    }

    // SEASON

    @Theory
    public void testHarvest_withSeason_reportSentForApproval(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestSeason(new HarvestSeason());
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(specVersion.supportsHarvestReport(), harvest, specVersion);

        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withSeason_reportApproved(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestSeason(new HarvestSeason());
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withSeason_reportRejected(final HarvestSpecVersion specVersion) {
        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestSeason(new HarvestSeason());
        harvest.setHarvestReportState(HarvestReportState.REJECTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    // PERMIT

    // accepted for permit

    @Theory
    public void testHarvest_withPermit_accepted(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_accepted_asContactPerson(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(true, harvest, specVersion);
    }

    // accepted for permit with normal harvest report

    @Theory
    public void testHarvest_withPermit_accepted_withReportSentForApproval(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_accepted_withReportSentForApproval_asContactPerson(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(true, harvest, specVersion);
    }

    @Theory
    public void testHarvest_withPermit_accepted_withReportApproved(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_accepted_withReportApproved_asContactPerson(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
    }

    @Theory
    public void testHarvest_withPermit_accepted_withReportRejected(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.REJECTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_accepted_withReportRejected_asContactPerson(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.REJECTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
    }

    // accepted for permit with end of hunting report

    @Theory
    public void testHarvest_withPermit_accepted_endOfHuntingSentForApproval(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);
        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_accepted_endOfHuntingApproved(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);
        permit.setHarvestReportState(HarvestReportState.APPROVED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(false, harvest);
    }

    @Theory
    public void testHarvest_withPermit_accepted_endOfHuntingRejected(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setOriginalContactPerson(activePerson);
        permit.setHarvestReportState(HarvestReportState.REJECTED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        assertWebCanEdit(false, harvest);
        assertMobileCanEdit(false, harvest, specVersion);
        assertModeratorCanEdit(false, harvest);
    }

    // rejected for permit

    @Theory
    public void testHarvest_withPermit_rejected_endOfHuntingSentForApproval(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(true, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_rejected_endOfHuntingAccepted(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setHarvestReportState(HarvestReportState.APPROVED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(true, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }

    @Theory
    public void testHarvest_withPermit_rejected_endOfHuntingRejected(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = new HarvestPermit();
        permit.setHarvestReportState(HarvestReportState.REJECTED);

        final Harvest harvest = new Harvest();
        harvest.setAuthor(activePerson);
        harvest.setActor(activePerson);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);

        assertWebCanEdit(true, harvest);
        assertMobileCanEdit(true, harvest, specVersion);
        assertModeratorCanEdit(true, harvest);
    }
}
