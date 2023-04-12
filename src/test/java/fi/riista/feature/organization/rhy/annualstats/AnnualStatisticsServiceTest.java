package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

public class AnnualStatisticsServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnualStatisticsService service;

    @Resource
    private RhyAnnualStatisticsRepository repository;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testRefresh_updateGameDamageStatistics() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            MockTimeProvider.mockTime(new LocalDate(2020, 12, 31).toDate().getTime());

            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);
            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
            model().newGameDamageInspectionEvent(rhy, species);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                runInTransaction(() -> {
                    MockTimeProvider.mockTime(new LocalDate(2021, 3, 5).toDate().getTime());

                    service.refresh(statistics);

                    final RhyAnnualStatistics refreshed = repository.getOne(statistics.getId());
                    assertThat(refreshed, is(notNullValue()));

                    final GameDamageStatistics gameDamageStatistics = refreshed.getOrCreateGameDamage();
                    assertThat(gameDamageStatistics, is(notNullValue()));
                    assertThat(gameDamageStatistics.getMooselikeDamageInspectionLocations(), is(equalTo(0)));
                    assertThat(gameDamageStatistics.getLargeCarnivoreDamageInspectionLocations(), is(equalTo(1)));
                });
            });
        });
    }

    @Test
    public void testRefresh_updateGameDamageStatisticsNotRefreshable() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            MockTimeProvider.mockTime(new LocalDate(2020, 12, 31).toDate().getTime());

            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);
            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
            model().newGameDamageInspectionEvent(rhy, species);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                runInTransaction(() -> {
                    MockTimeProvider.mockTime(new LocalDate(2021, 3, 6).toDate().getTime());

                    service.refresh(statistics);

                    final RhyAnnualStatistics refreshed = repository.getOne(statistics.getId());
                    assertThat(refreshed, is(notNullValue()));

                    final GameDamageStatistics gameDamageStatistics = refreshed.getOrCreateGameDamage();
                    assertThat(gameDamageStatistics, is(notNullValue()));

                    final GameDamageStatistics originalGameDamageStatistics = statistics.getOrCreateGameDamage();
                    assertThat(gameDamageStatistics.getMooselikeDamageInspectionLocations(),
                            is(equalTo(originalGameDamageStatistics.getMooselikeDamageInspectionLocations())));
                    assertThat(gameDamageStatistics.getLargeCarnivoreDamageInspectionLocations(),
                            is(equalTo(originalGameDamageStatistics.getLargeCarnivoreDamageInspectionLocations())));
                });
            });
        });
    }

    @Test
    public void testIsRefreshable_15JanOnFriday() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final LocalDate deadline = new LocalDate(2021, 1, 15);
            MockTimeProvider.mockTime(deadline.minusYears(1).toDate().getTime());
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                MockTimeProvider.mockTime(deadline.toDate().getTime());

                final boolean isRefreshable = AnnualStatisticsService.isRefreshable(statistics);
                assertThat(isRefreshable, is(equalTo(true)));
            });
        });
    }

    @Test
    public void testIsRefreshable_15JanOnSaturday() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final LocalDate deadline = new LocalDate(2022, 1, 17);
            MockTimeProvider.mockTime(deadline.minusYears(1).toDate().getTime());
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                MockTimeProvider.mockTime(deadline.toDate().getTime());

                final boolean isRefreshable = AnnualStatisticsService.isRefreshable(statistics);
                assertThat(isRefreshable, is(equalTo(true)));
            });
        });
    }

    @Test
    public void testIsRefreshable_15JanOnSunday() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final LocalDate deadline = new LocalDate(2023, 1, 16);
            MockTimeProvider.mockTime(deadline.minusYears(1).toDate().getTime());
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                MockTimeProvider.mockTime(deadline.toDate().getTime());

                final boolean isRefreshable = AnnualStatisticsService.isRefreshable(statistics);
                assertThat(isRefreshable, is(equalTo(true)));
            });
        });
    }

    @Test
    public void testIsRefreshable_statisticsNotRefreshable15JanOnFriday() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final LocalDate deadline = new LocalDate(2021, 1, 16);
            MockTimeProvider.mockTime(deadline.minusYears(1).toDate().getTime());
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                MockTimeProvider.mockTime(deadline.toDate().getTime());

                final boolean isRefreshable = AnnualStatisticsService.isRefreshable(statistics);
                assertThat(isRefreshable, is(equalTo(false)));
            });
        });
    }

    @Test
    public void testIsRefreshable_statisticsNotRefreshable15JanOnSaturday() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final LocalDate deadline = new LocalDate(2022, 1, 18);
            MockTimeProvider.mockTime(deadline.minusYears(1).toDate().getTime());
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                MockTimeProvider.mockTime(deadline.toDate().getTime());

                final boolean isRefreshable = AnnualStatisticsService.isRefreshable(statistics);
                assertThat(isRefreshable, is(equalTo(false)));
            });
        });
    }

    @Test
    public void testIsRefreshable_statisticsNotRefreshable15JanOnSunday() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final LocalDate deadline = new LocalDate(2023, 1, 17);
            MockTimeProvider.mockTime(deadline.minusYears(1).toDate().getTime());
            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                MockTimeProvider.mockTime(deadline.toDate().getTime());

                final boolean isRefreshable = AnnualStatisticsService.isRefreshable(statistics);
                assertThat(isRefreshable, is(equalTo(false)));
            });
        });
    }
}

