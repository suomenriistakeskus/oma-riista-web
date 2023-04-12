package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_COUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SHOOTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_TRACKER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.OTHER_GAMEKEEPING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SHOOTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.TRACKER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.OTHER_HUNTER_TRAINING_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class OtherHunterTrainingStatisticsParticipantsTest {
    private OtherHunterTrainingStatistics otherHunterTrainingStatistics;

    @Before
    public void setUp() {
        otherHunterTrainingStatistics = new OtherHunterTrainingStatistics();
        otherHunterTrainingStatistics.setSmallCarnivoreHuntingTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableSmallCarnivoreHuntingTrainingEvents(1);
        otherHunterTrainingStatistics.setSmallCarnivoreHuntingTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableSmallCarnivoreHuntingTrainingParticipants(1);
        otherHunterTrainingStatistics.setGameCountingTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableGameCountingTrainingEvents(1);
        otherHunterTrainingStatistics.setGameCountingTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableGameCountingTrainingParticipants(1);
        otherHunterTrainingStatistics.setGamePopulationManagementTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableGamePopulationManagementTrainingEvents(1);
        otherHunterTrainingStatistics.setGamePopulationManagementTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableGamePopulationManagementTrainingParticipants(1);
        otherHunterTrainingStatistics.setGameEnvironmentalCareTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableGameEnvironmentalCareTrainingEvents(1);
        otherHunterTrainingStatistics.setGameEnvironmentalCareTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableGameEnvironmentalCareTrainingParticipants(1);
        otherHunterTrainingStatistics.setOtherGamekeepingTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableOtherGamekeepingTrainingEvents(1);
        otherHunterTrainingStatistics.setOtherGamekeepingTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableOtherGamekeepingTrainingParticipants(1);
        otherHunterTrainingStatistics.setShootingTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableShootingTrainingEvents(1);
        otherHunterTrainingStatistics.setShootingTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableShootingTrainingParticipants(1);
        otherHunterTrainingStatistics.setTrackerTrainingEvents(1);
        otherHunterTrainingStatistics.setNonSubsidizableTrackerTrainingEvents(1);
        otherHunterTrainingStatistics.setTrackerTrainingParticipants(1);
        otherHunterTrainingStatistics.setNonSubsidizableTrackerTrainingParticipants(1);

    }

    @Test
    public void testListMissingParticipants_OtherHunterTrainingStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();

        assertThat(response._1, is(OTHER_HUNTER_TRAINING_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_smallCarnivoreHuntingTrainingEventErrors() {
        otherHunterTrainingStatistics.setSmallCarnivoreHuntingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_gameCountingTrainingEventErrors() {
        otherHunterTrainingStatistics.setGameCountingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, GAME_COUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_gamePopulationManagementTrainingEventErrors() {
        otherHunterTrainingStatistics.setGamePopulationManagementTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_gameEnvironmentalCareTrainingEventErrors() {
        otherHunterTrainingStatistics.setGameEnvironmentalCareTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_otherGamekeepingTrainingEventErrors() {
        otherHunterTrainingStatistics.setOtherGamekeepingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, OTHER_GAMEKEEPING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_shootingTrainingEventErrors() {
        otherHunterTrainingStatistics.setShootingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, SHOOTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_trackerTrainingEventErrors() {
        otherHunterTrainingStatistics.setTrackerTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, TRACKER_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableSmallCarnivoreHuntingTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableSmallCarnivoreHuntingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableGameCountingTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableGameCountingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableGamePopulationManagementTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableGamePopulationManagementTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableGameEnvironmentalCareTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableGameEnvironmentalCareTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableOtherGamekeepingTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableOtherGamekeepingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableShootingTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableShootingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_SHOOTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableTrackerTrainingEventErrors() {
        otherHunterTrainingStatistics.setNonSubsidizableTrackerTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = otherHunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_TRACKER_TRAINING_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(OTHER_HUNTER_TRAINING_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
