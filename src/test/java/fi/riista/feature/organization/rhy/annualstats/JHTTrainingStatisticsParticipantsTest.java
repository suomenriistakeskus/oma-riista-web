package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_DAMAGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTING_CONTROL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SHOOTING_TEST_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.JHT_TRAINING_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class JHTTrainingStatisticsParticipantsTest {
    private JHTTrainingStatistics jhtTrainingStatistics;

    @Before
    public void setUp() {
        jhtTrainingStatistics = new JHTTrainingStatistics();
        jhtTrainingStatistics.setShootingTestTrainingEvents(1);
        jhtTrainingStatistics.setNonSubsidizableShootingTestTrainingEvents(1);
        jhtTrainingStatistics.setShootingTestTrainingParticipants(1);
        jhtTrainingStatistics.setNonSubsidizableShootingTestTrainingParticipants(1);
        jhtTrainingStatistics.setHunterExamOfficialTrainingEvents(1);
        jhtTrainingStatistics.setNonSubsidizableHunterExamOfficialTrainingEvents(1);
        jhtTrainingStatistics.setHunterExamOfficialTrainingParticipants(1);
        jhtTrainingStatistics.setNonSubsidizableHunterExamOfficialTrainingParticipants(1);
        jhtTrainingStatistics.setGameDamageTrainingEvents(1);
        jhtTrainingStatistics.setNonSubsidizableGameDamageTrainingEvents(1);
        jhtTrainingStatistics.setGameDamageTrainingParticipants(1);
        jhtTrainingStatistics.setNonSubsidizableGameDamageTrainingParticipants(1);
        jhtTrainingStatistics.setHuntingControlTrainingEvents(1);
        jhtTrainingStatistics.setNonSubsidizableHuntingControlTrainingEvents(1);
        jhtTrainingStatistics.setHuntingControlTrainingParticipants(1);
        jhtTrainingStatistics.setNonSubsidizableHuntingControlTrainingParticipants(1);
    }

    @Test
    public void testListMissingParticipants_JHTTrainingStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();

        assertThat(response._1, is(JHT_TRAINING_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_shootingTestTrainingEventErrors() {
        jhtTrainingStatistics.setShootingTestTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, SHOOTING_TEST_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_hunterExamOfficialTrainingEventErrors() {
        jhtTrainingStatistics.setHunterExamOfficialTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_gameDamageTrainingEventErrors() {
        jhtTrainingStatistics.setGameDamageTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, GAME_DAMAGE_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_huntingControlTrainingEventErrors() {
        jhtTrainingStatistics.setHuntingControlTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, HUNTING_CONTROL_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableShootingTestTrainingEventErrors() {
        jhtTrainingStatistics.setNonSubsidizableShootingTestTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableHunterExamOfficialTrainingEventErrors() {
        jhtTrainingStatistics.setNonSubsidizableHunterExamOfficialTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableGameDamageTrainingEventErrors() {
        jhtTrainingStatistics.setNonSubsidizableGameDamageTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableHuntingControlTrainingEventErrors() {
        jhtTrainingStatistics.setNonSubsidizableHuntingControlTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = jhtTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(JHT_TRAINING_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
