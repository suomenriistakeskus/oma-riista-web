package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.PUBLIC_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.PUBLIC_EVENT_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class PublicEventStatisticsParticipantsTest {
    private PublicEventStatistics publicEventStatistics;

    @Before
    public void setUp() {
        publicEventStatistics = new PublicEventStatistics();
        publicEventStatistics.setPublicEvents(1);
        publicEventStatistics.setPublicEventParticipants(1);
    }

    @Test
    public void testListMissingParticipants_PublicEventStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = publicEventStatistics.listMissingParticipants();

        assertThat(response._1, is(PUBLIC_EVENT_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_publicEventErrors() {
        publicEventStatistics.setPublicEventParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = publicEventStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, PUBLIC_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(PUBLIC_EVENT_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
