package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryRepository;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertAppearance;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertMooseHuntingAreaType;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertTrendOfPopulationGrowth;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertTrendOfPopulationGrowthOfFlyDeer;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.Asserts.assertSuccess;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MooseDataCardHuntingSummaryTransfererTest extends EmbeddedDatabaseTest {

    private static final String INVALID = "invalid";

    @Resource
    private MooseDataCardHuntingSummaryTransferer transferer;

    @Resource
    private MooseHuntingSummaryRepository summaryRepo;

    @Test
    @Transactional
    public void testUpsertHuntingSummaryData_forCreate() {
        final HarvestPermit permit = model().newHarvestPermit();
        final HuntingClub club = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(club);

        persistInCurrentlyOpenTransaction();

        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCard();

        assertSuccess(transferer.upsertHuntingSummaryData(card, club, permit), summaryOpt -> {
            assertTrue(summaryOpt.isPresent());

            final MooseHuntingSummary summary = summaryOpt.get();
            assertEquals(Collections.singletonList(summary), summaryRepo.findAll());
            assertEquals(club, summary.getClub());
            assertEquals(permit, summary.getHarvestPermit());
        });
    }

    @Test
    @Transactional
    public void testUpsertHuntingSummaryData_forCreate_whenSummaryDataNotAvailable() {
        final HarvestPermit permit = model().newHarvestPermit();
        final HuntingClub club = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(club);

        persistInCurrentlyOpenTransaction();

        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCardWithoutSummary();

        assertSuccess(transferer.upsertHuntingSummaryData(card, club, permit), summaryOpt -> {
            assertFalse(summaryOpt.isPresent());
            assertEmpty(summaryRepo.findAll());
        });
    }

    @Test
    @Transactional
    public void testUpsertHuntingSummaryData_forUpdate() {
        final HarvestPermit permit = model().newHarvestPermit();
        final HuntingClub club = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(club);

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInCurrentlyOpenTransaction();

        final LocalDate today = today();
        final MooseHuntingSummary original = model().newMooseHuntingSummary(permit, club, false);
        original.setBeginDate(today.minusDays(2));
        original.setEndDate(today.minusDays(1));

        persistInCurrentlyOpenTransaction();

        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCard();

        assertSuccess(transferer.upsertHuntingSummaryData(card, club, permit), summaryOpt -> {
            assertTrue(summaryOpt.isPresent());

            final MooseHuntingSummary updated = summaryOpt.get();
            assertEquals(Collections.singletonList(updated), summaryRepo.findAll());
            assertEquals(Integer.valueOf(1), updated.getConsistencyVersion());
            assertEquals(club, updated.getClub());
            assertEquals(permit, updated.getHarvestPermit());
        });
    }

    @Test
    @Transactional
    public void testUpsertHuntingSummaryData_forUpdate_whenSummaryDataNotAvailable() {
        final HarvestPermit permit = model().newHarvestPermit();
        final HuntingClub club = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(club);

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInCurrentlyOpenTransaction();

        model().newMooseHuntingSummary(permit, club, false);

        persistInCurrentlyOpenTransaction();

        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCardWithoutSummary();

        assertSuccess(transferer.upsertHuntingSummaryData(card, club, permit), summaryOpt -> {
            assertTrue(summaryOpt.isPresent());

            final MooseHuntingSummary refreshed = summaryOpt.get();
            assertEquals(Collections.singletonList(refreshed), summaryRepo.findAll());
            assertEquals(Integer.valueOf(0), refreshed.getConsistencyVersion());
        });
    }

    @Test
    public void testTransferForPage7_withCompleteValidInput() {
        final MooseDataCardPage7 page7 = MooseDataCardObjectFactory.newPage7();
        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCard(page7);

        final MooseHuntingSummary summary = new MooseHuntingSummary();
        transferer.transferSummaryData(card, summary);

        assertNotNull(summary.getWhiteTailedDeerAppearance());
        assertEquals(Boolean.TRUE, summary.getWhiteTailedDeerAppearance().getAppeared());
        assertEquals(
                page7.getEstimatedSpecimenAmountOfWhiteTailedDeer(),
                summary.getWhiteTailedDeerAppearance().getEstimatedAmountOfSpecimens());
        assertEquals(
                convertTrendOfPopulationGrowth(page7.getTrendOfWhiteTailedDeerPopulationGrowth()),
                summary.getWhiteTailedDeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getRoeDeerAppearance());
        assertEquals(Boolean.TRUE, summary.getRoeDeerAppearance().getAppeared());
        assertEquals(
                page7.getEstimatedSpecimenAmountOfRoeDeer(),
                summary.getRoeDeerAppearance().getEstimatedAmountOfSpecimens());
        assertEquals(
                convertTrendOfPopulationGrowth(page7.getTrendOfRoeDeerPopulationGrowth()),
                summary.getRoeDeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getWildForestReindeerAppearance());
        assertEquals(Boolean.TRUE, summary.getWildForestReindeerAppearance().getAppeared());
        assertEquals(
                page7.getEstimatedSpecimenAmountOfWildForestReindeer(),
                summary.getWildForestReindeerAppearance().getEstimatedAmountOfSpecimens());
        assertEquals(
                convertTrendOfPopulationGrowth(page7.getTrendOfWildForestReindeerPopulationGrowth()),
                summary.getWildForestReindeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getFallowDeerAppearance());
        assertEquals(Boolean.TRUE, summary.getFallowDeerAppearance().getAppeared());
        assertEquals(
                page7.getEstimatedSpecimenAmountOfFallowDeer(),
                summary.getFallowDeerAppearance().getEstimatedAmountOfSpecimens());
        assertEquals(
                convertTrendOfPopulationGrowth(page7.getTrendOfFallowDeerPopulationGrowth()),
                summary.getFallowDeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getWildBoarAppearance());
        assertEquals(Boolean.TRUE, summary.getWildBoarAppearance().getAppeared());
        assertEquals(
                page7.getEstimatedSpecimenAmountOfWildBoar(),
                summary.getWildBoarAppearance().getEstimatedAmountOfSpecimens());
        assertEquals(
                convertTrendOfPopulationGrowth(page7.getTrendOfWildBoarPopulationGrowth()),
                summary.getWildBoarAppearance().getTrendOfPopulationGrowth());
    }

    @Test
    public void testTransferForPage7_forNullingOfInvalidValues() {
        final MooseDataCardPage7 page7 = MooseDataCardObjectFactory.newPage7()
                .withEstimatedSpecimenAmountOfWhiteTailedDeer(-1)
                .withTrendOfWhiteTailedDeerPopulationGrowth(INVALID)
                .withEstimatedSpecimenAmountOfRoeDeer(-1)
                .withTrendOfRoeDeerPopulationGrowth(INVALID)
                .withEstimatedSpecimenAmountOfWildForestReindeer(-1)
                .withTrendOfWildForestReindeerPopulationGrowth(INVALID)
                .withEstimatedSpecimenAmountOfFallowDeer(-1)
                .withTrendOfFallowDeerPopulationGrowth(INVALID)
                .withEstimatedSpecimenAmountOfWildBoar(-1)
                .withTrendOfWildBoarPopulationGrowth(INVALID)
                .withEstimatedAmountOfSowsWithPiglets(-1);

        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCard(page7);

        final MooseHuntingSummary summary = new MooseHuntingSummary();
        transferer.transferSummaryData(card, summary);

        assertNotNull(summary.getWhiteTailedDeerAppearance());
        assertEquals(Boolean.TRUE, summary.getWhiteTailedDeerAppearance().getAppeared());
        assertNull(summary.getWhiteTailedDeerAppearance().getEstimatedAmountOfSpecimens());
        assertNull(summary.getWhiteTailedDeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getRoeDeerAppearance());
        assertEquals(Boolean.TRUE, summary.getRoeDeerAppearance().getAppeared());
        assertNull(summary.getRoeDeerAppearance().getEstimatedAmountOfSpecimens());
        assertNull(summary.getRoeDeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getWildForestReindeerAppearance());
        assertEquals(Boolean.TRUE, summary.getWildForestReindeerAppearance().getAppeared());
        assertNull(summary.getWildForestReindeerAppearance().getEstimatedAmountOfSpecimens());
        assertNull(summary.getWildForestReindeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getFallowDeerAppearance());
        assertEquals(Boolean.TRUE, summary.getFallowDeerAppearance().getAppeared());
        assertNull(summary.getFallowDeerAppearance().getEstimatedAmountOfSpecimens());
        assertNull(summary.getFallowDeerAppearance().getTrendOfPopulationGrowth());

        assertNotNull(summary.getWildBoarAppearance());
        assertEquals(Boolean.TRUE, summary.getWildBoarAppearance().getAppeared());
        assertNull(summary.getWildBoarAppearance().getEstimatedAmountOfSpecimens());
        assertNull(summary.getWildBoarAppearance().getTrendOfPopulationGrowth());
        assertNull(summary.getWildBoarAppearance().getEstimatedAmountOfSowWithPiglets());
    }

    @Test
    public void testTransferForPage8_withCompleteValidInput() {
        Stream.of(MooseDataCardGameSpeciesAppearance.values()).forEach(appearance -> {

            final MooseDataCardPage8 page8 = MooseDataCardObjectFactory.newPage8();
            final MooseDataCardSection_8_4 section84 = page8.getSection_8_4().withDeerFlyAppearead(appearance);

            final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCard(page8);
            final MooseHuntingSummary summary = new MooseHuntingSummary();
            transferer.transferSummaryData(card, summary);

            assertHuntingEndDate(summary, page8);
            assertTransferOfSection81(summary, page8.getSection_8_1());
            assertTransferOfSection83(summary, page8.getSection_8_3());

            assertEquals(section84.getMooseHeatBeginDate(), summary.getMooseHeatBeginDate());
            assertEquals(section84.getMooseHeatEndDate(), summary.getMooseHeatEndDate());
            assertEquals(section84.getMooseFawnBeginDate(), summary.getMooseFawnBeginDate());
            assertEquals(section84.getMooseFawnEndDate(), summary.getMooseFawnEndDate());

            assertEquals(convertAppearance(appearance), summary.getDeerFliesAppeared());

            if (appearance == MooseDataCardGameSpeciesAppearance.NO) {
                assertNull(summary.getDateOfFirstDeerFlySeen());
                assertNull(summary.getDateOfLastDeerFlySeen());
                assertNull(summary.getNumberOfAdultMoosesHavingFlies());
                assertNull(summary.getNumberOfYoungMoosesHavingFlies());
                assertNull(summary.getTrendOfDeerFlyPopulationGrowth());
            } else {
                assertEquals(section84.getDateOfFirstDeerFlySeen(), summary.getDateOfFirstDeerFlySeen());
                assertEquals(section84.getDateOfLastDeerFlySeen(), summary.getDateOfLastDeerFlySeen());
                assertEquals(
                        section84.getNumberOfAdultMoosesHavingFlies(), summary.getNumberOfAdultMoosesHavingFlies());
                assertEquals(
                        section84.getNumberOfYoungMoosesHavingFlies(), summary.getNumberOfYoungMoosesHavingFlies());
                assertEquals(
                        MooseDataCardExtractor
                                .convertTrendOfPopulationGrowthOfFlyDeer(section84.getTrendOfDeerFlyPopulationGrowth()),
                        summary.getTrendOfDeerFlyPopulationGrowth());
            }
        });
    }

    @Test
    public void testTransferForPage8_forNullingOfInvalidValues() {
        final MooseDataCardPage8 page8 = MooseDataCardObjectFactory.newPage8();
        final MooseDataCardSection_8_4 section84 = page8.getSection_8_4()
                .withNumberOfAdultMoosesHavingFlies(-1)
                .withNumberOfYoungMoosesHavingFlies(-1);
        section84.setMooseHeatEndDate(section84.getMooseHeatBeginDate().minusDays(1));
        section84.setMooseFawnEndDate(section84.getMooseFawnBeginDate().minusDays(1));
        section84.setDateOfLastDeerFlySeen(section84.getDateOfFirstDeerFlySeen().minusDays(1));

        final MooseDataCard card = MooseDataCardObjectFactory.newMooseDataCard(page8);
        final MooseHuntingSummary summary = new MooseHuntingSummary();
        transferer.transferSummaryData(card, summary);

        assertHuntingEndDate(summary, page8);
        assertTransferOfSection81(summary, page8.getSection_8_1());
        assertTransferOfSection83(summary, page8.getSection_8_3());

        assertEquals(section84.getMooseHeatBeginDate(), summary.getMooseHeatBeginDate());
        assertNull(summary.getMooseHeatEndDate());
        assertEquals(section84.getMooseFawnBeginDate(), summary.getMooseFawnBeginDate());
        assertNull(summary.getMooseFawnEndDate());
        assertEquals(Boolean.TRUE, summary.getDeerFliesAppeared());
        assertEquals(section84.getDateOfFirstDeerFlySeen(), summary.getDateOfFirstDeerFlySeen());
        assertNull(summary.getDateOfLastDeerFlySeen());
        assertNull(summary.getNumberOfAdultMoosesHavingFlies());
        assertNull(summary.getNumberOfYoungMoosesHavingFlies());
        assertEquals(
                convertTrendOfPopulationGrowthOfFlyDeer(section84.getTrendOfDeerFlyPopulationGrowth()),
                summary.getTrendOfDeerFlyPopulationGrowth());
    }

    private static void assertHuntingEndDate(final MooseHuntingSummary summary, final MooseDataCardPage8 page8) {
        assertEquals(page8.getHuntingEndDate(), summary.getHuntingEndDate());
        assertTrue(summary.isHuntingFinished());
    }

    private static void assertTransferOfSection81(final MooseHuntingSummary summary,
                                                  final MooseDataCardSection_8_1 section81) {

        final AreaSizeAndRemainingPopulation areaAndPopulation = summary.getAreaSizeAndPopulation();
        assertNotNull(areaAndPopulation);
        assertEquals(section81.getTotalHuntingArea().intValue(), areaAndPopulation.getTotalHuntingArea().intValue());
        assertEquals(
                section81.getEffectiveHuntingArea().intValue(), areaAndPopulation.getEffectiveHuntingArea().intValue());
        assertEquals(
                section81.getMoosesRemainingInTotalHuntingArea(),
                areaAndPopulation.getRemainingPopulationInTotalArea());
        assertEquals(
                section81.getMoosesRemainingInEffectiveHuntingArea(),
                areaAndPopulation.getRemainingPopulationInEffectiveArea());

        assertNull(summary.getEffectiveHuntingAreaPercentage());
        assertEquals(convertMooseHuntingAreaType(section81.getHuntingAreaType()), summary.getHuntingAreaType());
    }

    private static void assertTransferOfSection83(final MooseHuntingSummary summary,
                                                  final MooseDataCardSection_8_3 section83) {

        assertEquals(section83.getNumberOfDrownedMooses(), summary.getNumberOfDrownedMooses());
        assertEquals(section83.getNumberOfMoosesKilledByBear(), summary.getNumberOfMoosesKilledByBear());
        assertEquals(section83.getNumberOfMoosesKilledByWolf(), summary.getNumberOfMoosesKilledByWolf());
        assertEquals(
                section83.getNumberOfMoosesKilledInTrafficAccident(),
                summary.getNumberOfMoosesKilledInTrafficAccident());
        assertEquals(section83.getNumberOfMoosesKilledInPoaching(), summary.getNumberOfMoosesKilledByPoaching());
        assertEquals(section83.getNumberOfMoosesKilledInRutFight(), summary.getNumberOfMoosesKilledInRutFight());
        assertEquals(section83.getNumberOfStarvedMooses(), summary.getNumberOfStarvedMooses());
        assertEquals(
                section83.getNumberOfMoosesDeceasedByOtherReason(), summary.getNumberOfMoosesDeceasedByOtherReason());
        assertEquals(section83.getExplanationForOtherReason(), summary.getCauseOfDeath());
    }
}
