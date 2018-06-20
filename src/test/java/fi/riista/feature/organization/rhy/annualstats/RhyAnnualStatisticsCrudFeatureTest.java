package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.currentYear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RhyAnnualStatisticsCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private RhyAnnualStatisticsCrudFeature feature;

    @Test
    public void testGetOrCreate_whenNotExists() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            // Create another RHY to test that annual statistics is created for correct RHY.
            model().newRiistanhoitoyhdistys();

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                final long rhyId = rhy.getId();
                final int currentYear = currentYear();

                final RhyAnnualStatisticsDTO dto = feature.getOrCreate(rhyId, currentYear);

                assertNotNull(dto);
                assertEquals(rhyId, dto.getRhyId());
                assertEquals(currentYear, dto.getYear());
                assertEquals(RhyAnnualStatisticsState.IN_PROGRESS, dto.getState());
            });
        });
    }

    @Test
    public void testGetOrCreate_whenExists() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

            // Create another annual statistics to test that the correct one is returned.
            model().newRhyAnnualStatistics(model().newRiistanhoitoyhdistys());

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                final long rhyId = rhy.getId();
                final int year = statistics.getYear();

                final RhyAnnualStatisticsDTO dto = feature.getOrCreate(rhyId, year);

                assertNotNull(dto);
                assertEquals(statistics.getId(), dto.getId());
                assertEquals(rhyId, dto.getRhyId());
                assertEquals(year, dto.getYear());
                assertEquals(RhyAnnualStatisticsState.IN_PROGRESS, dto.getState());
            });
        });
    }

    @Test
    public void testUpdate_smokeTestWithNoChanges() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            final RhyAnnualStatistics annualStats = model().newRhyAnnualStatistics(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> invokeUpdate(annualStats));
        });
    }

    @Test(expected = Exception.class)
    public void testUpdate_expectedToFailWhenUnderInspection() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);
            statistics.setState(RhyAnnualStatisticsState.UNDER_INSPECTION);

            onSavedAndAuthenticated(createUser(coordinator), () -> invokeUpdate(statistics));
        });
    }

    private void invokeUpdate(final RhyAnnualStatistics statistics) {
        final RhyAnnualStatisticsDTO dto = RhyAnnualStatisticsDTO.create(statistics);
        dto.setSrva(null);

        feature.update(dto);
    }
}
