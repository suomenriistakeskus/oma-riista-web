package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
}

