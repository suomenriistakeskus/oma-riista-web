package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTING_CONTROLLERS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTING_CONTROL_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class HuntingControlStatisticsParticipantsTest {
    private HuntingControlStatistics huntingControlStatistics;

    @Before
    public void setUp() {
        huntingControlStatistics = new HuntingControlStatistics();
        huntingControlStatistics.setHuntingControlEvents(1);
        huntingControlStatistics.setNonSubsidizableHuntingControlEvents(1);
        huntingControlStatistics.setHuntingControlCustomers(1);
        huntingControlStatistics.setProofOrders(1);
        huntingControlStatistics.setHuntingControllers(1);

    }

    @Test
    public void testListMissingParticipants_HuntingControlStatistics_successfully() {

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = huntingControlStatistics.listMissingParticipants();

        assertThat(response._1, is(HUNTING_CONTROL_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_HunterExamTrainingStatistics_huntingControllersErrors() {
        huntingControlStatistics.setHuntingControllers(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = huntingControlStatistics.listMissingParticipants();
        assertOneErrorInResponse(response, HUNTING_CONTROLLERS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(HUNTING_CONTROL_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }
}
