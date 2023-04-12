package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.COLLEGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_COLLEGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SCHOOL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.OTHER_YOUTH_TARGETED_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SCHOOL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.YOUTH_TRAINING_STATISTICS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class YouthTrainingStatisticsParticipantsTest {
    private YouthTrainingStatistics youthTrainingStatistics;

    @Before
    public void setUp() {
        youthTrainingStatistics = new YouthTrainingStatistics();
        youthTrainingStatistics.setSchoolTrainingEvents(1);
        youthTrainingStatistics.setNonSubsidizableSchoolTrainingEvents(1);
        youthTrainingStatistics.setSchoolTrainingParticipants(1);
        youthTrainingStatistics.setNonSubsidizableSchoolTrainingParticipants(1);
        youthTrainingStatistics.setCollegeTrainingEvents(1);
        youthTrainingStatistics.setNonSubsidizableCollegeTrainingEvents(1);
        youthTrainingStatistics.setCollegeTrainingParticipants(1);
        youthTrainingStatistics.setNonSubsidizableCollegeTrainingParticipants(1);
        youthTrainingStatistics.setOtherYouthTargetedTrainingEvents(1);
        youthTrainingStatistics.setNonSubsidizableOtherYouthTargetedTrainingEvents(1);
        youthTrainingStatistics.setOtherYouthTargetedTrainingParticipants(1);
        youthTrainingStatistics.setNonSubsidizableOtherYouthTargetedTrainingParticipants(1);
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_successfully() {
        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertThat(response._1, is(YOUTH_TRAINING_STATISTICS));
        assertThat(response._2, is(empty()));
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_schoolTrainingEventErrors() {
        youthTrainingStatistics.setSchoolTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, SCHOOL_TRAINING_EVENTS);
    }

    private void assertOneErrorInResponse(final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response, final AnnualStatisticsParticipantField participantField) {
        assertThat(response._1, is(YOUTH_TRAINING_STATISTICS));
        assertThat(response._2.size(), is(1));
        assertThat(response._2, containsInAnyOrder(
                participantField));
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_collegeTrainingEventErrors() {
        youthTrainingStatistics.setCollegeTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, COLLEGE_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_otherYouthTargetedTrainingEventErrors() {
        youthTrainingStatistics.setOtherYouthTargetedTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, OTHER_YOUTH_TARGETED_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_nonSubsidizableSchoolTrainingEventErrors() {
        youthTrainingStatistics.setNonSubsidizableSchoolTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_SCHOOL_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_nonSubsidizableCollegeTrainingEventErrors() {
        youthTrainingStatistics.setNonSubsidizableCollegeTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_COLLEGE_TRAINING_EVENTS);
    }

    @Test
    public void testListMissingParticipants_YouthTrainingStatistics_nonSubsidizableOtherYouthTargetedTrainingEventErrors() {
        youthTrainingStatistics.setNonSubsidizableOtherYouthTargetedTrainingParticipants(0);

        final Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> response = youthTrainingStatistics.listMissingParticipants();

        assertOneErrorInResponse(response, NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_EVENTS);
    }
}
