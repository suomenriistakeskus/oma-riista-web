package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportService;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiConsumer;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class RhySubsidyStatisticsExportServiceTest extends EmbeddedDatabaseTest
        implements RhyAnnualStatisticsTestDataPopulator {

    @Resource
    private RhySubsidyStatisticsExportService weightedExportService;

    @Resource
    private AnnualStatisticsExportService statisticsExportService;

    @Test
    public void testWeighting_2019() {
        populateStatistics(2018);

        exportAndAssert(2019, (dto, weighted) -> {
            assertThat(weighted.getLuke().isEqualTo(dto.getLuke()), is(true));
            assertOtherStatsMatch(dto, weighted);
        });
    }

    @Test
    public void testWeighting_2020() {
        populateStatistics(2019);

        exportAndAssert(2020, (dto, weighted) -> {
            assertThat(weighted.getLuke().isEqualTo(dto.getLuke()), is(true));
            assertOtherStatsMatch(dto, weighted);
        });
    }

    @Test
    public void testWeighting_2021() {
        populateStatistics(2020);

        exportAndAssert(2021, (dto, weighted) -> {
            final LukeStatistics luke = dto.getLuke();
            final LukeStatistics weightedLuke = weighted.getLuke();

            assertThat(weightedLuke.getSummerGameTriangles(), equalTo(luke.getSummerGameTriangles() * 3));
            assertThat(weightedLuke.getWinterGameTriangles(), equalTo(luke.getWinterGameTriangles() * 3));
            assertThat(weightedLuke.getWaterBirdCouples(), equalTo(luke.getWaterBirdCouples()));
            assertThat(weightedLuke.getFieldTriangles(), equalTo(luke.getFieldTriangles()));
            assertThat(weightedLuke.getWaterBirdBroods(), equalTo(luke.getWaterBirdBroods()));
            assertThat(weightedLuke.getCarnivoreContactPersons(), equalTo(luke.getCarnivoreContactPersons()));

            assertOtherStatsMatch(dto, weighted);
        });
    }

    private void exportAndAssert(final int subsidyYear,
                                 final BiConsumer<AnnualStatisticsExportDTO, AnnualStatisticsExportDTO> assertion) {
        final int statisticsYear = subsidyYear - 1;

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final EnumSet<RhyAnnualStatisticsState> states = EnumSet.of(APPROVED);
            final List<AnnualStatisticsExportDTO> annualStatistics =
                    statisticsExportService.exportAnnualStatistics(statisticsYear, states);
            final List<AnnualStatisticsExportDTO> weightedAnnualStatistics =
                    weightedExportService.exportWeightedAnnualStatistics(subsidyYear, statisticsYear, states);
            assertThat(annualStatistics, hasSize(1));
            assertThat(weightedAnnualStatistics, hasSize(1));

            assertion.accept(annualStatistics.get(0), weightedAnnualStatistics.get(0));
        });

    }

    private static void assertOtherStatsMatch(final AnnualStatisticsExportDTO dto,
                                              final AnnualStatisticsExportDTO weighted) {

        assertThat(weighted.getBasicInfo().isEqualTo(dto.getBasicInfo()), is(true));

        assertThat(weighted.getHunterExams().isEqualTo(dto.getHunterExams()), is(true));
        assertThat(weighted.getShootingTests().isEqualTo(dto.getShootingTests()), is(true));
        assertThat(weighted.getGameDamage().isEqualTo(dto.getGameDamage()), is(true));
        assertThat(weighted.getHuntingControl().isEqualTo(dto.getHuntingControl()), is(true));
        assertThat(weighted.getOtherPublicAdmin().isEqualTo(dto.getOtherPublicAdmin()), is(true));

        assertThat(weighted.getSrva().countAllSrvaEvents(), equalTo(dto.getSrva().countAllSrvaEvents()));
        assertThat(weighted.getSrva().countMooselikes(), equalTo(dto.getSrva().countMooselikes()));

        assertThat(weighted.getHunterExamTraining().isEqualTo(dto.getHunterExamTraining()), is(true));
        assertThat(weighted.getJhtTraining().isEqualTo(dto.getJhtTraining()), is(true));
        assertThat(weighted.getHunterTraining().isEqualTo(dto.getHunterTraining()), is(true));
        assertThat(weighted.getYouthTraining().isEqualTo(dto.getYouthTraining()), is(true));
        assertThat(weighted.getOtherHunterTraining().isEqualTo(dto.getOtherHunterTraining()), is(true));
        assertThat(weighted.getPublicEvents().isEqualTo(dto.getPublicEvents()), is(true));

        assertThat(weighted.getOtherHuntingRelated().isEqualTo(dto.getOtherHuntingRelated()), is(true));
        assertThat(weighted.getCommunication().isEqualTo(dto.getCommunication()), is(true));
        assertThat(weighted.getShootingRanges().isEqualTo(dto.getShootingRanges()), is(true));
        assertThat(weighted.getMetsahallitus().isEqualTo(dto.getMetsahallitus()), is(true));
    }


    private void populateStatistics(final int statisticsYear) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy);

        populate(statistics);
        statistics.setYear(statisticsYear);
        statistics.setState(APPROVED);
    }

}
