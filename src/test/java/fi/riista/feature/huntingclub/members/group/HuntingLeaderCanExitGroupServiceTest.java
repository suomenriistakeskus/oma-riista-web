package fi.riista.feature.huntingclub.members.group;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HuntingLeaderCanExitGroupServiceTest extends EmbeddedDatabaseTest {
    @Resource
    private HuntingLeaderCanExitGroupService service;

    private void assertHuntingLeaderIsLocked(final Occupation occupation,
                                             final String errorMessage) {
        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            assertTrue(errorMessage, service.isHuntingLeaderLocked(occupation));
        });
    }

    private void assertHuntingLeaderNotLocked(final Occupation occupation,
                                              final String errorMessage) {
        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            assertFalse(errorMessage, service.isHuntingLeaderLocked(occupation));
        });
    }

    @Test
    public void testGroupHasActiveMember() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation member = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_JASEN);
        final Occupation huntingLeader = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertHuntingLeaderIsLocked(huntingLeader, "should NOT ALLOW delete hunting leader when group has active member");
    }

    @Test
    public void testAnotherHuntingLeader_NotExists_EmptyGroup() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation occupation = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertHuntingLeaderNotLocked(occupation, "should ALLOW delete hunting leader for group without data");
    }

    @Test
    public void testAnotherHuntingLeader_Exists_NotEmptyGroup() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation member = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_JASEN);
        final Occupation huntingLeader1 = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation huntingLeader2 = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertHuntingLeaderNotLocked(huntingLeader1, "should ALLOW delete hunting leader when another hunting leader exists");
    }

    @Test
    public void testAnotherHuntingLeader_OccupationNotActive_EmptyGroup() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation huntingLeader1 = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation huntingLeader2 = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        huntingLeader2.setEndDate(DateUtil.today().minusDays(1));

        assertHuntingLeaderNotLocked(huntingLeader1, "should ALLOW delete hunting leader when group has members and other hunting leader is not valid");
    }

    @Test
    public void testAnotherHuntingLeader_OccupationNotActive_NotEmptyGroup() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation member = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_JASEN);
        final Occupation huntingLeader1 = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation huntingLeader2 = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        huntingLeader2.setEndDate(DateUtil.today().minusDays(1));

        assertHuntingLeaderIsLocked(huntingLeader1, "should NOT ALLOW delete hunting leader when group has members and other hunting leader is not valid");
    }

    @Test
    public void testClubHasHuntingDaysForMooseGroup() {
        final HuntingClubGroup group = model().newHuntingClubGroup(model().newGameSpeciesMoose());
        final Occupation huntingLeader = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        model().newGroupHuntingDay(group, DateUtil.today());

        assertHuntingLeaderIsLocked(huntingLeader, "should NOT ALLOW delete hunting leader when group has hunting days for moose group");
    }

    @Test
    public void testClubHasHarvestLinkedToHuntingDay() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation huntingLeader = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());
        final Harvest harvest = model().newHarvest();
        harvest.updateHuntingDayOfGroup(huntingDay, null);

        assertHuntingLeaderIsLocked(huntingLeader, "should NOT ALLOW delete hunting leader when group has harvest linked to hunting day");
    }

    @Test
    public void testClubHasObservationLinkedToHuntingDay_mooseHunting() {
        testClubHasObservationLinkedToHuntingDayWithObservation(MOOSE_HUNTING);
    }

    @Test
    public void testClubHasObservationLinkedToHuntingDay_deerHunting() {
        testClubHasObservationLinkedToHuntingDayWithObservation(DEER_HUNTING);
    }

    private void testClubHasObservationLinkedToHuntingDayWithObservation(final ObservationCategory category) {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation huntingLeader = model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());
        final Observation observation = model().newObservation();
        observation.setObservationCategory(category);
        observation.updateHuntingDayOfGroup(huntingDay, null);

        assertHuntingLeaderIsLocked(huntingLeader, "should NOT ALLOW delete hunting leader when group has observation linked to hunting day");
    }

}
