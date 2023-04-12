package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTER_EXAM_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTER_EXAM_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class HunterExamStatisticsParticipantsTest {
    private HunterExamStatistics hunterExamStatistics;

    @Before
    public void setUp() {
        hunterExamStatistics = new HunterExamStatistics();
        hunterExamStatistics.setHunterExamEvents(1);
        hunterExamStatistics.setPassedHunterExams(1);
        hunterExamStatistics.setFailedHunterExams(1);
        hunterExamStatistics.setHunterExamOfficials(1);
    }

    @Test
    public void testListMissingParticipants_HunterExamStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterExamStatistics.listMissingParticipants();

        assertThat(response._1, is(HUNTER_EXAM_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamStatistics_hunterExamEventErrors() {
        hunterExamStatistics.setPassedHunterExams(0);
        hunterExamStatistics.setFailedHunterExams(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = hunterExamStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, HUNTER_EXAM_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(HUNTER_EXAM_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
