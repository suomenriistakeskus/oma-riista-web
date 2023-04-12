package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTER_EXAM_TRAINING_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class HunterExamTrainingStatisticsParticipantsTest {
    private HunterExamTrainingStatistics hunterExamTrainingStatistics;

    @Before
    public void setUp() {
        hunterExamTrainingStatistics = new HunterExamTrainingStatistics();
        hunterExamTrainingStatistics.setHunterExamTrainingEvents(1);
        hunterExamTrainingStatistics.setNonSubsidizableHunterExamTrainingEvents(1);
        hunterExamTrainingStatistics.setHunterExamTrainingParticipants(1);
        hunterExamTrainingStatistics.setNonSubsidizableHunterExamTrainingParticipants(1);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterExamTrainingStatistics.listMissingParticipants();

        assertThat(response._1, is(HUNTER_EXAM_TRAINING_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_hunterExamTrainingEventErrors() {
        hunterExamTrainingStatistics.setHunterExamTrainingParticipants(0);


        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterExamTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, HUNTER_EXAM_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_nonSubsidizableHunterExamTrainingEventErrors() {
        hunterExamTrainingStatistics.setNonSubsidizableHunterExamTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterExamTrainingStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(HUNTER_EXAM_TRAINING_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
