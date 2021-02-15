package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO.createForHarvest;
import static fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO.createForObservation;
import static fi.riista.util.DateUtil.today;

public class GroupHuntingDiaryFeature_RejectTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    @Resource
    private GroupHuntingDiaryFeature feature;

    // Harvest

    @Test(expected = NotFoundException.class)
    public void testRejectHarvestFromHuntingGroup_whenGroupDoesNotExist() {
        feature.rejectDiaryEntryFromHuntingGroup(createForHarvest(123456789, -1));
    }

    @Test(expected = NotFoundException.class)
    public void testRejectHarvestFromHuntingGroup_whenHarvestDoesNotExist() {
        withMooseHuntingGroupFixture(fixture -> {
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () ->
                    feature.rejectDiaryEntryFromHuntingGroup(createForHarvest(123456789, fixture.group.getId())));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testRejectHarvestFromHuntingGroup_whenHarvestIsNotRelatedToGroup() {
        withMooseHuntingGroupFixture(fixture -> {
            final Harvest harvest = model().newHarvest(fixture.species);
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () ->
                    feature.rejectDiaryEntryFromHuntingGroup(createForHarvest(harvest.getId(), fixture.group.getId())));
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testRejectHarvestFromHuntingGroup_whenHuntingFinished() {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Harvest harvest = model().newHarvest(fixture.species, fixture.clubContact, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, null);

            // Set club hunting finished.
            persistInNewTransaction();
            model().newMooseHuntingSummary(fixture.permit, fixture.club, true);

            onSavedAndAuthenticated(createUser(fixture.clubContact),
                    () -> feature.rejectDiaryEntryFromHuntingGroup(createForHarvest(harvest.getId(), fixture.group.getId())));
        });
    }

    // Observation

    @Test(expected = NotFoundException.class)
    public void testRejectObservationFromHuntingGroup_whenGroupDoesNotExist() {
        feature.rejectDiaryEntryFromHuntingGroup(createForObservation(123456789, -1));
    }

    @Test(expected = NotFoundException.class)
    public void testRejectObservationFromHuntingGroup_whenObservationDoesNotExist() {
        withMooseHuntingGroupFixture(fixture -> {
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () ->
                    feature.rejectDiaryEntryFromHuntingGroup(createForObservation(123456789, fixture.group.getId())));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testRejectObservationFromHuntingGroup_whenObservationIsNotRelatedToGroup() {
        withMooseHuntingGroupFixture(fixture -> {
            final Observation observation = model().newObservation(fixture.species);
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () ->
                    feature.rejectDiaryEntryFromHuntingGroup(createForObservation(observation.getId(), fixture.group.getId())));
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testRejectObservationFromHuntingGroup_whenHuntingFinished() {
        createObservationMetaF(MOOSE_HUNTING, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), fixture -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            final Observation observation = model().newObservation(fixture.species, fixture.clubContact, huntingDay);

            // Set club hunting finished.
            model().newBasicHuntingSummary(fixture.speciesAmount, fixture.club, true);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () ->
                    feature.rejectDiaryEntryFromHuntingGroup(createForObservation(observation.getId(), fixture.group.getId())));
        }));
    }
}
