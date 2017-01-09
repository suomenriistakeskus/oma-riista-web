package fi.riista.feature.pub.statistics;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import org.junit.Test;

import javax.annotation.Resource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PublicHarvestPivotTableFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PublicHarvestPivotTableFeature pivotTableFeature;

    private final AtomicInteger officalCodeSequence = new AtomicInteger(1);

    @Test
    public void testSummary_smoke() {
        withRhy(rhy1 -> withRhy(rhy2 -> withRhy(rhy3 -> withRhy(rhy4 -> {

            GameSpecies species1 = model().newGameSpecies();
            model().newHarvest(species1, GameAge.YOUNG, GameGender.MALE, rhy1);
            model().newHarvest(species1, GameAge.YOUNG, GameGender.FEMALE, rhy2);
            model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy3);
            model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy4);

            persistInNewTransaction();

            PublicHarvestPivotTableFeature.PivotTable summary = pivotTableFeature.summary(
                    species1.getOfficialCode(), today(), today());

            assertNotNull(summary.getData());
            assertNotNull(summary.getGrandTotal());
            assertNotNull(summary.getGrandTotal().getOfficialCode());
            assertEquals(4, summary.getData().size());

            summary.getData().forEach(row -> {
                assertNotNull(row.getOfficialCode());
                assertNotNull(row.getNameFinnish());
                assertNotNull(row.getNameSwedish());
            });
            assertEquals(Riistakeskus.OFFICIAL_CODE, summary.getGrandTotal().getOfficialCode());
        }))));
    }

    @Test
    public void testSummary_withDifferentTimeRanges() {
        withRhy(rhy1 -> withRhy(rhy2 -> {
            GameSpecies species1 = model().newGameSpecies();

            // Harvest for today
            model().newHarvest(species1, GameAge.YOUNG, GameGender.MALE, rhy1);
            model().newHarvest(species1, GameAge.YOUNG, GameGender.FEMALE, rhy2);

            persistInNewTransaction();

            // Now until one year
            PublicHarvestPivotTableFeature.PivotTable s1 = pivotTableFeature.summary(
                    species1.getOfficialCode(), today(), today().plusYears(1));

            assertEquals(2, s1.getData().size());

            // One year before until now
            PublicHarvestPivotTableFeature.PivotTable s2 = pivotTableFeature.summary(
                    species1.getOfficialCode(), today().minusYears(1), today());

            assertEquals(2, s2.getData().size());

            // From tomorrow
            PublicHarvestPivotTableFeature.PivotTable s3 = pivotTableFeature.summary(
                    species1.getOfficialCode(), today().plusDays(1), today().plusYears(1));

            assertEquals(0, s3.getData().size());

            // Until yesterday
            PublicHarvestPivotTableFeature.PivotTable s4 = pivotTableFeature.summary(
                    species1.getOfficialCode(), today().minusYears(1), today().minusDays(1));

            assertEquals(0, s4.getData().size());
        }));
    }

    @Test
    public void testSummary_withNullSpecies() {
        withRhy(rhy -> {
            model().newHarvest(model().newGameSpecies(), GameAge.ADULT, null, rhy);
            model().newHarvest(model().newGameSpecies(), GameAge.YOUNG, GameGender.FEMALE, rhy);

            persistInNewTransaction();

            PublicHarvestPivotTableFeature.PivotTable summary = pivotTableFeature.summary(null, today(), today());

            assertEquals(2, summary.getGrandTotal().getTotal());
            assertEquals(1, summary.getGrandTotal().getAgeAdult());
            assertEquals(1, summary.getGrandTotal().getAgeYoung());
            assertEquals(0, summary.getGrandTotal().getAgeUnknown());
            assertEquals(1, summary.getGrandTotal().getGenderFemale());
            assertEquals(0, summary.getGrandTotal().getGenderMale());
            assertEquals(1, summary.getGrandTotal().getGenderUnknown());
        });
    }

    @Test
    public void testSummary_withOneSpeciesAndOneRka() {
        withRhy(rhy1 -> withRhy(rhy2 -> withRhy(rhy3 -> withRhy(rhy4 -> withRhy(rhy5 -> withRhy(rhy6 -> {

            GameSpecies species1 = model().newGameSpecies();
            model().newHarvest(species1, GameAge.YOUNG, GameGender.MALE, rhy1);
            model().newHarvest(species1, GameAge.YOUNG, GameGender.FEMALE, rhy2);
            model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy3);
            model().newHarvest(species1, GameAge.ADULT, GameGender.UNKNOWN, rhy4);
            model().newHarvest(species1, GameAge.UNKNOWN, GameGender.MALE, rhy5);
            model().newHarvest(species1, GameAge.UNKNOWN, GameGender.UNKNOWN, rhy6);

            persistInNewTransaction();

            PublicHarvestPivotTableFeature.PivotTable summary = pivotTableFeature.summary(
                    species1.getOfficialCode(), today(), today());

            assertEquals(6, summary.getGrandTotal().getTotal());
            assertEquals(2, summary.getGrandTotal().getAgeAdult());
            assertEquals(2, summary.getGrandTotal().getAgeYoung());
            assertEquals(2, summary.getGrandTotal().getAgeUnknown());
            assertEquals(1, summary.getGrandTotal().getGenderFemale());
            assertEquals(3, summary.getGrandTotal().getGenderMale());
            assertEquals(2, summary.getGrandTotal().getGenderUnknown());
        }))))));
    }

    @Test
    public void testSummary_withNullValues() {
        withRhy(rhy1 -> withRhy(rhy2 -> withRhy(rhy3 -> {

            GameSpecies species1 = model().newGameSpecies();
            model().newHarvest(species1, GameAge.YOUNG, null, rhy1);
            model().newHarvest(species1, null, GameGender.MALE, rhy2);
            model().newHarvest(species1, GameAge.UNKNOWN, null, rhy3);

            persistInNewTransaction();

            PublicHarvestPivotTableFeature.PivotTable summary = pivotTableFeature.summary(
                    species1.getOfficialCode(), today(), today());

            assertEquals(3, summary.getGrandTotal().getTotal());
            assertEquals(0, summary.getGrandTotal().getAgeAdult());
            assertEquals(1, summary.getGrandTotal().getAgeYoung());
            assertEquals(2, summary.getGrandTotal().getAgeUnknown());
            assertEquals(0, summary.getGrandTotal().getGenderFemale());
            assertEquals(1, summary.getGrandTotal().getGenderMale());
            assertEquals(2, summary.getGrandTotal().getGenderUnknown());
        })));
    }

    @Test
    public void testSummaryForRka_withSingleAndMultipleSpecimenHarvests() {
        RiistakeskuksenAlue alue1 = newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy1 = newRhy(alue1);
        Riistanhoitoyhdistys rhy2 = newRhy(alue1);

        GameSpecies species = model().newGameSpecies();
        Harvest h1 = model().newHarvest(species, GameAge.ADULT, GameGender.MALE, rhy1);
        model().newHarvestSpecimen(h1, GameAge.ADULT, GameGender.MALE);
        h1.setAmount(2);

        model().newHarvest(species, GameAge.ADULT, GameGender.MALE, rhy2);

        persistInNewTransaction();

        PublicHarvestPivotTableFeature.PivotTable summary =
                pivotTableFeature.summaryForRka(species.getOfficialCode(), alue1.getOfficialCode(), today(), today());

        assertEquals(3, summary.getGrandTotal().getTotal());
        assertEquals(3, summary.getGrandTotal().getAgeAdult());
        assertEquals(0, summary.getGrandTotal().getAgeYoung());
        assertEquals(0, summary.getGrandTotal().getAgeUnknown());
        assertEquals(0, summary.getGrandTotal().getGenderFemale());
        assertEquals(3, summary.getGrandTotal().getGenderMale());
        assertEquals(0, summary.getGrandTotal().getGenderUnknown());
    }

    @Test
    public void testSummary_withUndefinedSpecimens() {
        RiistakeskuksenAlue alue1 = newRiistakeskuksenAlue();
        RiistakeskuksenAlue alue2 = newRiistakeskuksenAlue();
        GameSpecies species = model().newGameSpecies();

        Riistanhoitoyhdistys rhy1 = newRhy(alue1);
        Harvest harvest1 = model().newHarvest(species, rhy1);
        model().newHarvestSpecimen(harvest1, GameAge.YOUNG, null);
        model().newHarvestSpecimen(harvest1, null, GameGender.MALE);
        harvest1.setAmount(2);

        Riistanhoitoyhdistys rhy2 = newRhy(alue1);
        Harvest harvest2 = model().newHarvest(species, rhy2);
        harvest2.setAmount(4);

        Riistanhoitoyhdistys rhy3 = newRhy(alue2);
        Harvest harvest3 = model().newHarvest(species, rhy3);
        model().newHarvestSpecimen(harvest3, GameAge.ADULT, null);
        model().newHarvestSpecimen(harvest3, null, GameGender.FEMALE);
        model().newHarvestSpecimen(harvest3, GameAge.ADULT, GameGender.MALE);
        harvest3.setAmount(5);

        persistInNewTransaction();

        PublicHarvestPivotTableFeature.PivotTable summary =
                pivotTableFeature.summary(species.getOfficialCode(), today(), today());

        int rka1Amount = harvest1.getAmount() + harvest2.getAmount();
        int rka2Amount = harvest3.getAmount();
        int totalAmount = rka1Amount + rka2Amount;

        assertEquals(totalAmount, summary.getGrandTotal().getTotal());
        assertEquals(2, summary.getGrandTotal().getAgeAdult());
        assertEquals(1, summary.getGrandTotal().getAgeYoung());
        assertEquals(totalAmount - 3, summary.getGrandTotal().getAgeUnknown());
        assertEquals(1, summary.getGrandTotal().getGenderFemale());
        assertEquals(2, summary.getGrandTotal().getGenderMale());
        assertEquals(totalAmount - 3, summary.getGrandTotal().getGenderUnknown());

        // Ordered by officialCode
        PublicHarvestPivotTableFeature.PivotTableRow row1 = summary.getData().get(0);
        PublicHarvestPivotTableFeature.PivotTableRow row2 = summary.getData().get(1);

        assertEquals(alue1.getOfficialCode(), row1.getOfficialCode());
        assertEquals(rka1Amount, row1.getTotal());
        assertEquals(0, row1.getAgeAdult());
        assertEquals(1, row1.getAgeYoung());
        assertEquals(rka1Amount - 1, row1.getAgeUnknown());
        assertEquals(0, row1.getGenderFemale());
        assertEquals(1, row1.getGenderMale());
        assertEquals(rka1Amount - 1, row1.getGenderUnknown());

        assertEquals(alue2.getOfficialCode(), row2.getOfficialCode());
        assertEquals(rka2Amount, row2.getTotal());
        assertEquals(2, row2.getAgeAdult());
        assertEquals(0, row2.getAgeYoung());
        assertEquals(rka2Amount - 2, row2.getAgeUnknown());
        assertEquals(1, row2.getGenderFemale());
        assertEquals(1, row2.getGenderMale());
        assertEquals(rka2Amount - 2, row2.getGenderUnknown());
    }

    @Test
    public void testSummaryForRka_withUndefinedSpecimens() {
        RiistakeskuksenAlue alue = newRiistakeskuksenAlue();
        GameSpecies species = model().newGameSpecies();

        Riistanhoitoyhdistys rhy1 = newRhy(alue);
        Harvest harvest1 = model().newHarvest(species, rhy1);
        model().newHarvestSpecimen(harvest1, GameAge.YOUNG, null);
        model().newHarvestSpecimen(harvest1, null, GameGender.MALE);
        harvest1.setAmount(2);

        Riistanhoitoyhdistys rhy2 = newRhy(alue);
        Harvest harvest2 = model().newHarvest(species, rhy2);
        model().newHarvestSpecimen(harvest2, GameAge.ADULT, null);
        model().newHarvestSpecimen(harvest2, null, GameGender.FEMALE);
        model().newHarvestSpecimen(harvest2, GameAge.ADULT, GameGender.MALE);
        harvest2.setAmount(5);

        Riistanhoitoyhdistys rhy3 = newRhy(alue);
        Harvest harvest3 = model().newHarvest(species, rhy3);
        harvest3.setAmount(4);

        persistInNewTransaction();

        PublicHarvestPivotTableFeature.PivotTable summary =
                pivotTableFeature.summaryForRka(species.getOfficialCode(), alue.getOfficialCode(), today(), today());

        int totalAmount = harvest1.getAmount() + harvest2.getAmount() + harvest3.getAmount();

        assertEquals(totalAmount, summary.getGrandTotal().getTotal());
        assertEquals(2, summary.getGrandTotal().getAgeAdult());
        assertEquals(1, summary.getGrandTotal().getAgeYoung());
        assertEquals(totalAmount - 3, summary.getGrandTotal().getAgeUnknown());
        assertEquals(1, summary.getGrandTotal().getGenderFemale());
        assertEquals(2, summary.getGrandTotal().getGenderMale());
        assertEquals(totalAmount - 3, summary.getGrandTotal().getGenderUnknown());

        // Ordered by officialCode
        PublicHarvestPivotTableFeature.PivotTableRow row1 = summary.getData().get(0);
        PublicHarvestPivotTableFeature.PivotTableRow row2 = summary.getData().get(1);
        PublicHarvestPivotTableFeature.PivotTableRow row3 = summary.getData().get(2);

        assertEquals(rhy1.getOfficialCode(), row1.getOfficialCode());
        assertEquals(harvest1.getAmount(), row1.getTotal());
        assertEquals(0, row1.getAgeAdult());
        assertEquals(1, row1.getAgeYoung());
        assertEquals(harvest1.getAmount() - row1.getAgeAdult() - row1.getAgeYoung(), row1.getAgeUnknown());
        assertEquals(0, row1.getGenderFemale());
        assertEquals(1, row1.getGenderMale());
        assertEquals(harvest1.getAmount() - row1.getGenderFemale() - row1.getGenderMale(), row1.getGenderUnknown());

        assertEquals(rhy2.getOfficialCode(), row2.getOfficialCode());
        assertEquals(harvest2.getAmount(), row2.getTotal());
        assertEquals(2, row2.getAgeAdult());
        assertEquals(0, row2.getAgeYoung());
        assertEquals(harvest2.getAmount() - row2.getAgeAdult() - row2.getAgeYoung(), row2.getAgeUnknown());
        assertEquals(1, row2.getGenderFemale());
        assertEquals(1, row2.getGenderMale());
        assertEquals(harvest2.getAmount() - row2.getGenderFemale() - row2.getGenderMale(), row2.getGenderUnknown());

        assertEquals(rhy3.getOfficialCode(), row3.getOfficialCode());
        assertEquals(harvest3.getAmount(), row3.getTotal());
        assertEquals(0, row3.getAgeAdult());
        assertEquals(0, row3.getAgeYoung());
        assertEquals(harvest3.getAmount() - row3.getAgeAdult() - row3.getAgeYoung(), row3.getAgeUnknown());
        assertEquals(0, row3.getGenderFemale());
        assertEquals(0, row3.getGenderMale());
        assertEquals(harvest3.getAmount() - row3.getGenderFemale() - row3.getGenderMale(), row3.getGenderUnknown());
    }

    @Test
    public void testSummary_withTwoSpecies() {
        RiistakeskuksenAlue alue1 = newRiistakeskuksenAlue();
        RiistakeskuksenAlue alue2 = newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy1 = newRhy(alue1);
        Riistanhoitoyhdistys rhy2 = newRhy(alue1);
        Riistanhoitoyhdistys rhy3 = newRhy(alue1);
        Riistanhoitoyhdistys rhy4 = newRhy(alue2);

        GameSpecies species1 = model().newGameSpecies();
        model().newHarvest(species1, GameAge.YOUNG, GameGender.MALE, rhy1);
        model().newHarvest(species1, GameAge.YOUNG, GameGender.FEMALE, rhy2);
        model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy3);
        model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy4);

        // Wrong species, should not be counted
        GameSpecies species2 = model().newGameSpecies();
        model().newHarvest(species2, GameAge.ADULT, GameGender.MALE, rhy1);
        model().newHarvest(species2, GameAge.YOUNG, GameGender.FEMALE, rhy2);

        persistInNewTransaction();

        PublicHarvestPivotTableFeature.PivotTable summary = pivotTableFeature.summary(
                species1.getOfficialCode(), today(), today());

        assertEquals(2, summary.getData().size());
        assertEquals(4, summary.getGrandTotal().getTotal());
        assertEquals(2, summary.getGrandTotal().getAgeAdult());
        assertEquals(2, summary.getGrandTotal().getAgeYoung());
        assertEquals(0, summary.getGrandTotal().getAgeUnknown());
        assertEquals(1, summary.getGrandTotal().getGenderFemale());
        assertEquals(3, summary.getGrandTotal().getGenderMale());
        assertEquals(0, summary.getGrandTotal().getGenderUnknown());

        // Ordered by officialCode
        PublicHarvestPivotTableFeature.PivotTableRow row1 = summary.getData().get(0);
        PublicHarvestPivotTableFeature.PivotTableRow row2 = summary.getData().get(1);

        assertEquals(alue1.getOfficialCode(), row1.getOfficialCode());
        assertEquals(3, row1.getTotal());
        assertEquals(1, row1.getAgeAdult());
        assertEquals(2, row1.getAgeYoung());
        assertEquals(0, row1.getAgeUnknown());
        assertEquals(1, row1.getGenderFemale());
        assertEquals(2, row1.getGenderMale());
        assertEquals(0, row1.getGenderUnknown());

        assertEquals(alue2.getOfficialCode(), row2.getOfficialCode());
        assertEquals(1, row2.getTotal());
        assertEquals(1, row2.getAgeAdult());
        assertEquals(0, row2.getAgeYoung());
        assertEquals(0, row2.getAgeUnknown());
        assertEquals(0, row2.getGenderFemale());
        assertEquals(1, row2.getGenderMale());
        assertEquals(0, row2.getGenderUnknown());
    }

    @Test
    public void testSummaryForRka_withMultipleRkas() {
        RiistakeskuksenAlue alue1 = newRiistakeskuksenAlue();
        RiistakeskuksenAlue alue2 = newRiistakeskuksenAlue();
        newRiistakeskuksenAlue();
        newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy1 = newRhy(alue1);
        Riistanhoitoyhdistys rhy2 = newRhy(alue1);
        Riistanhoitoyhdistys rhy3 = newRhy(alue1);
        Riistanhoitoyhdistys rhy4 = newRhy(alue2);
        Riistanhoitoyhdistys rhy5 = newRhy(alue2);
        Riistanhoitoyhdistys rhy6 = newRhy(alue2);

        GameSpecies species1 = model().newGameSpecies();
        model().newHarvest(species1, GameAge.YOUNG, GameGender.MALE, rhy1);
        model().newHarvest(species1, GameAge.YOUNG, GameGender.FEMALE, rhy1);
        model().newHarvest(species1, GameAge.UNKNOWN, GameGender.FEMALE, rhy2);
        model().newHarvest(species1, GameAge.ADULT, GameGender.FEMALE, rhy2);
        model().newHarvest(species1, GameAge.ADULT, GameGender.UNKNOWN, rhy3);
        model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy3);

        // Wrong RKA
        model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy4);
        model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy5);
        model().newHarvest(species1, GameAge.ADULT, GameGender.MALE, rhy6);

        persistInNewTransaction();

        PublicHarvestPivotTableFeature.PivotTable summary = pivotTableFeature.summaryForRka(
                species1.getOfficialCode(), alue1.getOfficialCode(), today(), today());

        // 3 RHY
        assertEquals(3, summary.getData().size());

        assertEquals(6, summary.getGrandTotal().getTotal());
        assertEquals(3, summary.getGrandTotal().getAgeAdult());
        assertEquals(2, summary.getGrandTotal().getAgeYoung());
        assertEquals(1, summary.getGrandTotal().getAgeUnknown());
        assertEquals(3, summary.getGrandTotal().getGenderFemale());
        assertEquals(2, summary.getGrandTotal().getGenderMale());
        assertEquals(1, summary.getGrandTotal().getGenderUnknown());

        // Ordered by officialCode
        PublicHarvestPivotTableFeature.PivotTableRow row1 = summary.getData().get(0);
        PublicHarvestPivotTableFeature.PivotTableRow row2 = summary.getData().get(1);
        PublicHarvestPivotTableFeature.PivotTableRow row3 = summary.getData().get(2);

        assertEquals(rhy1.getOfficialCode(), row1.getOfficialCode());
        assertEquals(2, row1.getTotal());
        assertEquals(0, row1.getAgeAdult());
        assertEquals(2, row1.getAgeYoung());
        assertEquals(0, row1.getAgeUnknown());
        assertEquals(1, row1.getGenderFemale());
        assertEquals(1, row1.getGenderMale());
        assertEquals(0, row1.getGenderUnknown());

        assertEquals(rhy2.getOfficialCode(), row2.getOfficialCode());
        assertEquals(2, row2.getTotal());
        assertEquals(1, row2.getAgeAdult());
        assertEquals(0, row2.getAgeYoung());
        assertEquals(1, row2.getAgeUnknown());
        assertEquals(2, row2.getGenderFemale());
        assertEquals(0, row2.getGenderMale());
        assertEquals(0, row2.getGenderUnknown());

        assertEquals(rhy3.getOfficialCode(), row3.getOfficialCode());
        assertEquals(2, row3.getTotal());
        assertEquals(2, row3.getAgeAdult());
        assertEquals(0, row3.getAgeYoung());
        assertEquals(0, row3.getAgeUnknown());
        assertEquals(0, row3.getGenderFemale());
        assertEquals(1, row3.getGenderMale());
        assertEquals(1, row3.getGenderUnknown());
    }

    @Override
    public void withRhy(Consumer<Riistanhoitoyhdistys> testBody) {
        testBody.accept(newRhy());
    }

    private RiistakeskuksenAlue newRiistakeskuksenAlue() {
        return model().newRiistakeskuksenAlue(nextOfficialCode());
    }

    private Riistanhoitoyhdistys newRhy(final RiistakeskuksenAlue rka) {
        return model().newRiistanhoitoyhdistys(rka, nextOfficialCode());
    }

    private Riistanhoitoyhdistys newRhy() {
        return model().newRiistanhoitoyhdistys(newRiistakeskuksenAlue(), nextOfficialCode());
    }

    private String nextOfficialCode() {
        return String.valueOf(officalCodeSequence.incrementAndGet());
    }

}
