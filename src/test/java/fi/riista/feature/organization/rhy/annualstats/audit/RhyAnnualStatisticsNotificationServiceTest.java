package fi.riista.feature.organization.rhy.annualstats.audit;

import com.google.common.collect.ImmutableSortedMap;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsStateChangeEvent;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedMap;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.NOT_STARTED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.BASIC_INFO;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.COMMUNICATION;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.GAME_DAMAGE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAMS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAM_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTING_CONTROL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.JHT_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.METSAHALLITUS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTING_RELATED;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_PUBLIC_ADMIN_TASKS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.PUBLIC_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_TESTS;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class RhyAnnualStatisticsNotificationServiceTest extends EmbeddedDatabaseTest {

    @DataPoints
    public static final EnumSet<AnnualStatisticGroup> editableGroups = EnumSet.of(
            BASIC_INFO, HUNTER_EXAMS, SHOOTING_TESTS, GAME_DAMAGE, HUNTING_CONTROL, OTHER_PUBLIC_ADMIN_TASKS,
            HUNTER_EXAM_TRAINING, JHT_TRAINING, HUNTER_TRAINING, OTHER_HUNTER_TRAINING, PUBLIC_EVENTS,
            OTHER_HUNTING_RELATED, COMMUNICATION, SHOOTING_RANGES, LUKE, METSAHALLITUS);

    @Resource
    private RhyAnnualStatisticsNotificationService service;

    @Theory
    public void testFindAnnualStatisticGroupsUpdatedByModerator_verifySameGroupIncludedOnlyOnce(final AnnualStatisticGroup group) {
        final DateTime startOfToday = DateUtil.toDateTimeNullSafe(today());
        final DateTime startOfYesterday = startOfToday.minusDays(1);

        final AnnualStatisticGroup group2 = someOtherThan(group);

        withRhy(rhy -> {
            final RhyAnnualStatistics stats = model().newRhyAnnualStatistics(rhy);

            // Overridden by later event
            newModeratorUpdateEvent(stats, group, startOfYesterday.plusHours(1));

            final RhyAnnualStatisticsModeratorUpdateEvent event2 =
                    newModeratorUpdateEvent(stats, group, startOfYesterday.plusHours(2));
            final RhyAnnualStatisticsModeratorUpdateEvent event3 =
                    newModeratorUpdateEvent(stats, group2, startOfYesterday.plusHours(3));

            persistInNewTransaction();

            final List<AggregatedAnnualStatisticsModeratorUpdateDTO> result =
                    findModeratorUpdates(startOfYesterday, startOfToday);

            assertEquals(1, result.size());
            assertResult(stats, result.get(0), event2, event3);
        });
    }

    @Test
    public void testFindAnnualStatisticGroupsUpdatedByModerator_verifyTemporalFiltering() {
        final DateTime startOfToday = DateUtil.toDateTimeNullSafe(today());
        final DateTime startOfYesterday = startOfToday.minusDays(1);

        final AnnualStatisticGroup group = someEditableGroup();
        final AnnualStatisticGroup group2 = someOtherThan(group);

        withRhy(rhy -> {
            final RhyAnnualStatistics stats = model().newRhyAnnualStatistics(rhy);

            final RhyAnnualStatisticsModeratorUpdateEvent event1 =
                    newModeratorUpdateEvent(stats, group, startOfYesterday.plusSeconds(1));
            final RhyAnnualStatisticsModeratorUpdateEvent event2 = newModeratorUpdateEvent(stats, group2, startOfToday);

            // Outside of search interval
            newModeratorUpdateEvent(stats, group2, startOfYesterday);
            newModeratorUpdateEvent(stats, group2, startOfToday.plusSeconds(1));

            persistInNewTransaction();

            final List<AggregatedAnnualStatisticsModeratorUpdateDTO> result =
                    findModeratorUpdates(startOfYesterday, startOfToday);

            assertEquals(1, result.size());
            assertResult(stats, result.get(0), event1, event2);
        });
    }

    @Test
    public void testFindAnnualStatisticGroupsUpdatedByModerator_verifyGroupingByRhy() {
        final DateTime startOfToday = DateUtil.toDateTimeNullSafe(today());
        final DateTime startOfYesterday = startOfToday.minusDays(1);

        final AnnualStatisticGroup group = someEditableGroup();

        withRhy(rhy1 -> withRhy(rhy2 -> withRhy(rhy3 -> {
            final RhyAnnualStatistics stats1 = model().newRhyAnnualStatistics(rhy1);
            final RhyAnnualStatistics stats2 = model().newRhyAnnualStatistics(rhy2);
            final RhyAnnualStatistics stats3 = model().newRhyAnnualStatistics(rhy3);

            final RhyAnnualStatisticsModeratorUpdateEvent event1 =
                    newModeratorUpdateEvent(stats1, group, startOfYesterday.plusHours(1));
            final RhyAnnualStatisticsModeratorUpdateEvent event2 =
                    newModeratorUpdateEvent(stats2, group, startOfYesterday.plusHours(2));
            final RhyAnnualStatisticsModeratorUpdateEvent event3 =
                    newModeratorUpdateEvent(stats3, group, startOfYesterday.plusHours(3));

            persistInNewTransaction();

            final List<AggregatedAnnualStatisticsModeratorUpdateDTO> result =
                    findModeratorUpdates(startOfYesterday, startOfToday);

            assertEquals(3, result.size());
            assertResult(stats1, result.get(0), event1);
            assertResult(stats2, result.get(1), event2);
            assertResult(stats3, result.get(2), event3);
        })));
    }

    @Test
    public void testFindAnnualStatisticGroupsUpdatedByModerator_verifyGroupingByYear() {
        final int currentYear = currentYear();

        final DateTime startOfToday = DateUtil.toDateTimeNullSafe(today());
        final DateTime startOfYesterday = startOfToday.minusDays(1);

        final AnnualStatisticGroup group = someEditableGroup();

        withRhy(rhy -> {
            final RhyAnnualStatistics stats1 = model().newRhyAnnualStatistics(rhy, currentYear - 2);
            final RhyAnnualStatistics stats2 = model().newRhyAnnualStatistics(rhy, currentYear - 1);
            final RhyAnnualStatistics stats3 = model().newRhyAnnualStatistics(rhy, currentYear);

            final RhyAnnualStatisticsModeratorUpdateEvent event1 =
                    newModeratorUpdateEvent(stats1, group, startOfYesterday.plusHours(1));
            final RhyAnnualStatisticsModeratorUpdateEvent event2 =
                    newModeratorUpdateEvent(stats2, group, startOfYesterday.plusHours(2));
            final RhyAnnualStatisticsModeratorUpdateEvent event3 =
                    newModeratorUpdateEvent(stats3, group, startOfYesterday.plusHours(3));

            persistInNewTransaction();

            final List<AggregatedAnnualStatisticsModeratorUpdateDTO> result =
                    findModeratorUpdates(startOfYesterday, startOfToday);

            assertEquals(3, result.size());
            assertResult(stats1, result.get(0), event1);
            assertResult(stats2, result.get(1), event2);
            assertResult(stats3, result.get(2), event3);
        });
    }

    private AnnualStatisticGroup someEditableGroup() {
        return some(editableGroups);
    }

    private AnnualStatisticGroup someOtherThan(final AnnualStatisticGroup excluded) {
        return someOtherThan(excluded, editableGroups);
    }

    private RhyAnnualStatisticsModeratorUpdateEvent newModeratorUpdateEvent(final RhyAnnualStatistics statistics,
                                                                            final AnnualStatisticGroup group,
                                                                            final DateTime eventTime) {

        return model().newRhyAnnualStatisticsModeratorUpdateEvent(statistics, group, eventTime, getUserId());
    }

    private static long getUserId() {
        return ActiveUserService.SCHEDULED_TASK_USER_ID;
    }

    private List<AggregatedAnnualStatisticsModeratorUpdateDTO> findModeratorUpdates(final DateTime beginTime,
                                                                                    final DateTime endTime) {

        return service.findAnnualStatisticGroupsUpdatedByModerator(new Interval(beginTime, endTime));
    }

    private static SortedMap<AnnualStatisticGroup, DateTime> toMap(final RhyAnnualStatisticsModeratorUpdateEvent... events) {
        final ImmutableSortedMap.Builder<AnnualStatisticGroup, DateTime> builder = ImmutableSortedMap.naturalOrder();
        Arrays.stream(events).forEach(event -> builder.put(event.getDataGroup(), event.getEventTime()));
        return builder.build();
    }

    private static void assertResult(final RhyAnnualStatistics source,
                                     final AggregatedAnnualStatisticsModeratorUpdateDTO dto,
                                     final RhyAnnualStatisticsModeratorUpdateEvent... expectedEvents) {

        assertEquals(source.getRhy().getId().longValue(), dto.getRhyId());
        assertEquals(source.getYear(), dto.getYear());
        assertEquals(toMap(expectedEvents), dto.getDataGroups());
    }

    @Test
    public void testFindApprovedAnnualStatistics_withinInterval() {
        final RhyAnnualStatistics stats1 = model().newRhyAnnualStatistics();
        stats1.setState(APPROVED);

        newStateChangeEvent(stats1, NOT_STARTED);
        newStateChangeEvent(stats1, UNDER_INSPECTION);
        newStateChangeEvent(stats1, APPROVED);

        final RhyAnnualStatistics stats2 = model().newRhyAnnualStatistics();
        stats2.setState(UNDER_INSPECTION);

        newStateChangeEvent(stats2, NOT_STARTED);
        newStateChangeEvent(stats2, UNDER_INSPECTION);
        newStateChangeEvent(stats2, APPROVED);
        newStateChangeEvent(stats2, UNDER_INSPECTION); // approval cancelled

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final DateTime startOfToday = DateUtil.toDateTimeNullSafe(today());
            final DateTime startOfTomorrow = startOfToday.plusDays(1);

            assertEquals(singletonList(stats1), findApprovedStatistics(startOfToday, startOfTomorrow));
        });
    }

    @Test
    public void testFindApprovedAnnualStatistics_outsideInterval() {
        final RhyAnnualStatistics stats = model().newRhyAnnualStatistics();
        stats.setState(APPROVED);

        newStateChangeEvent(stats, NOT_STARTED);
        newStateChangeEvent(stats, UNDER_INSPECTION);
        newStateChangeEvent(stats, APPROVED);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final DateTime startOfToday = DateUtil.toDateTimeNullSafe(today());
            final DateTime startOfYesterday = startOfToday.minusDays(1);

            assertEmpty(findApprovedStatistics(startOfYesterday, startOfToday));
        });
    }

    private List<RhyAnnualStatistics> findApprovedStatistics(final DateTime beginTime, final DateTime endTime) {
        return service.findApprovedAnnualStatistics(new Interval(beginTime, endTime));
    }

    private RhyAnnualStatisticsStateChangeEvent newStateChangeEvent(final RhyAnnualStatistics statistics,
                                                                    final RhyAnnualStatisticsState state) {

        return model().newRhyAnnualStatisticsStateChangeEvent(statistics, state);
    }
}
