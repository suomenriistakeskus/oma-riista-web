package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.ACCIDENT_PREVENTION_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.MOOSELIKE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SRVA_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SRVA_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTER_TRAINING_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class HunterTrainingStatisticsParticipantsTest {
    private HunterTrainingStatistics hunterTrainingStatistics;

    @Before
    public void setUp() {
        hunterTrainingStatistics = new HunterTrainingStatistics();
        hunterTrainingStatistics.setMooselikeHuntingTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableMooselikeHuntingTrainingEvents(1);

        hunterTrainingStatistics.setMooselikeHuntingTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableMooselikeHuntingTrainingParticipants(1);

        hunterTrainingStatistics.setMooselikeHuntingLeaderTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableMooselikeHuntingLeaderTrainingEvents(1);

        hunterTrainingStatistics.setMooselikeHuntingLeaderTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableMooselikeHuntingLeaderTrainingParticipants(1);

        hunterTrainingStatistics.setCarnivoreHuntingTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableCarnivoreHuntingTrainingEvents(1);

        hunterTrainingStatistics.setCarnivoreHuntingTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableCarnivoreHuntingTrainingParticipants(1);

        hunterTrainingStatistics.setCarnivoreHuntingLeaderTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableCarnivoreHuntingLeaderTrainingEvents(1);

        hunterTrainingStatistics.setCarnivoreHuntingLeaderTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants(1);

        hunterTrainingStatistics.setSrvaTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableSrvaTrainingEvents(1);

        hunterTrainingStatistics.setSrvaTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableSrvaTrainingParticipants(1);

        hunterTrainingStatistics.setCarnivoreContactPersonTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableCarnivoreContactPersonTrainingEvents(1);

        hunterTrainingStatistics.setCarnivoreContactPersonTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableCarnivoreContactPersonTrainingParticipants(1);

        hunterTrainingStatistics.setAccidentPreventionTrainingEvents(1);
        hunterTrainingStatistics.setNonSubsidizableAccidentPreventionTrainingEvents(1);

        hunterTrainingStatistics.setAccidentPreventionTrainingParticipants(1);
        hunterTrainingStatistics.setNonSubsidizableAccidentPreventionTrainingParticipants(1);

    }

    @Test
    public void testListMissingParticipants_HunterTrainingStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();

        assertThat(response._1, is(HUNTER_TRAINING_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_mooselikeHuntingTrainingEventErrors() {
        hunterTrainingStatistics.setMooselikeHuntingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, MOOSELIKE_HUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_mooselikeHuntingLeaderTrainingEventErrors() {
        hunterTrainingStatistics.setMooselikeHuntingLeaderTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_carnivoreHuntingTrainingEventErrors() {
        hunterTrainingStatistics.setCarnivoreHuntingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, CARNIVORE_HUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_carnivoreHuntingLeaderTrainingEventErrors() {
        hunterTrainingStatistics.setCarnivoreHuntingLeaderTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_srvaTrainingEventErrors() {
        hunterTrainingStatistics.setSrvaTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, SRVA_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_carnivoreContactPersonTrainingEventErrors() {
        hunterTrainingStatistics.setCarnivoreContactPersonTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_accidentPreventionTrainingEventErrors() {
        hunterTrainingStatistics.setAccidentPreventionTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, ACCIDENT_PREVENTION_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableMooselikeHuntingTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableMooselikeHuntingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableMooselikeHuntingLeaderTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableMooselikeHuntingLeaderTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableCarnivoreHuntingTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableCarnivoreHuntingTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableCarnivoreHuntingLeaderTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableSrvaTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableSrvaTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_SRVA_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableCarnivoreContactPersonTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableCarnivoreContactPersonTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableAccidentPreventionTrainingEventErrors() {
        hunterTrainingStatistics.setNonSubsidizableAccidentPreventionTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(HUNTER_TRAINING_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
