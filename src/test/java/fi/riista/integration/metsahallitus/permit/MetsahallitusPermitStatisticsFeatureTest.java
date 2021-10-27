package fi.riista.integration.metsahallitus.permit;

import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;


public class MetsahallitusPermitStatisticsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MetsahallitusPermitStatisticsFeature feature;


    @Test
    public void testSmoke() {
        model().newMetsahallitusPermit();
        model().newMetsahallitusPermit();
        model().newMetsahallitusPermit();
        model().newMetsahallitusPermit();
        model().newMetsahallitusPermit();

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MetsahallitusPermitStatisticsDTO metsahallitusPermitStatisticsDTO =
                    feature.listStatistics(Locales.FI, null);

            assertEquals(0, metsahallitusPermitStatisticsDTO.getInvalidPeriodPermitCount());
            assertEquals(5, metsahallitusPermitStatisticsDTO.getHunterCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().size());
            assertEquals(5, metsahallitusPermitStatisticsDTO.getPermitCounts().get(0).getPermitCount());

        });

    }

    @Test
    public void testInvalidCounts() {
        model().newMetsahallitusPermit();
        model().newMetsahallitusPermit();

        final MetsahallitusPermit permit1 = model().newMetsahallitusPermit();
        permit1.setBeginDate(null);

        final MetsahallitusPermit permit2 = model().newMetsahallitusPermit();
        permit2.setEndDate(null);

        final MetsahallitusPermit permit3 = model().newMetsahallitusPermit();
        permit3.setBeginDate(null);
        permit3.setEndDate(null);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MetsahallitusPermitStatisticsDTO metsahallitusPermitStatisticsDTO =
                    feature.listStatistics(Locales.FI, null);

            assertEquals(3, metsahallitusPermitStatisticsDTO.getInvalidPeriodPermitCount());
            assertEquals(2, metsahallitusPermitStatisticsDTO.getHunterCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().size());
            assertEquals(2, metsahallitusPermitStatisticsDTO.getPermitCounts().get(0).getPermitCount());

        });

    }

    @Test
    public void testPermitTypeCounts() {
        createValidMhPermit("eka");
        createValidMhPermit("toka");
        createValidMhPermit("kolmas");

        final MetsahallitusPermit permit1 = model().newMetsahallitusPermit("eka", "area", model().hunterNumber());
        permit1.setBeginDate(null);

        final MetsahallitusPermit permit2 = model().newMetsahallitusPermit("toka", "area", model().hunterNumber());
        permit2.setEndDate(null);

        final MetsahallitusPermit permit3 = model().newMetsahallitusPermit("kolmas", "area", model().hunterNumber());
        permit3.setBeginDate(null);
        permit3.setEndDate(null);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MetsahallitusPermitStatisticsDTO metsahallitusPermitStatisticsDTO =
                    feature.listStatistics(Locales.FI, null);

            assertEquals(3, metsahallitusPermitStatisticsDTO.getInvalidPeriodPermitCount());
            assertEquals(3, metsahallitusPermitStatisticsDTO.getHunterCount());
            assertEquals(3, metsahallitusPermitStatisticsDTO.getPermitCounts().size());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(0).getPermitCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(1).getPermitCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(2).getPermitCount());

        });

    }

    @Test
    public void testHunterCount() {

        final String hunternumber1 = model().hunterNumber();
        final String hunternumber2 = model().hunterNumber();
        model().newMetsahallitusPermit("eka", "area", hunternumber1);
        model().newMetsahallitusPermit("toka", "area", hunternumber2);

        model().newMetsahallitusPermit("kolmas", "area", hunternumber2);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MetsahallitusPermitStatisticsDTO metsahallitusPermitStatisticsDTO =
                    feature.listStatistics(Locales.FI, null);

            assertEquals(0, metsahallitusPermitStatisticsDTO.getInvalidPeriodPermitCount());
            assertEquals(2, metsahallitusPermitStatisticsDTO.getHunterCount());
            assertEquals(3, metsahallitusPermitStatisticsDTO.getPermitCounts().size());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(0).getPermitCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(1).getPermitCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(2).getPermitCount());

        });

    }

    @Test
    public void testSwedishTypeMissingCount() {

        final MetsahallitusPermit eka = createValidMhPermit("eka");
        eka.setPermitTypeSwedish(null);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MetsahallitusPermitStatisticsDTO metsahallitusPermitStatisticsDTO =
                    feature.listStatistics(Locales.FI, null);

            assertEquals(1, metsahallitusPermitStatisticsDTO.getSwedishTypeMissingPermitCount());
            assertEquals(0, metsahallitusPermitStatisticsDTO.getInvalidPeriodPermitCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getHunterCount());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().size());
            assertEquals(1, metsahallitusPermitStatisticsDTO.getPermitCounts().get(0).getPermitCount());

        });

    }

    @Test
    public void testStatisticsForHuntingYear() {

        final MetsahallitusPermit permit2021 = createValidMhPermit("permit2021");
        permit2021.setBeginDate(new LocalDate(2021, 8, 1));
        permit2021.setEndDate(new LocalDate(2021, 8, 3));

        final MetsahallitusPermit permit2020 = createValidMhPermit("permit2020");
        permit2020.setBeginDate(new LocalDate(2021, 7, 30));
        permit2020.setEndDate(new LocalDate(2021, 7, 31)); // end date for hunting year 2020

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MetsahallitusPermitStatisticsDTO actual = feature.listStatistics(Locales.FI, 2021);
            assertThat(actual.getPermitCounts(), hasSize(1));
            assertThat(actual.getHunterCount(), is(1L));
            assertThat(actual.getInvalidPeriodPermitCount(), is(0L));
            assertThat(actual.getSwedishTypeMissingPermitCount(), is(0L));

            final MetsahallitusPermitStatisticsDTO.PermitCountDTO actualPermitCount = actual.getPermitCounts().get(0);
            assertThat(actualPermitCount.getPermitType(), is(permit2021.getPermitType()));
            assertThat(actualPermitCount.getPermitCount(), is(1L));
        });
    }

    private MetsahallitusPermit createValidMhPermit(final String type) {
        return model().newMetsahallitusPermit(type, "area", model().hunterNumber());
    }

}
