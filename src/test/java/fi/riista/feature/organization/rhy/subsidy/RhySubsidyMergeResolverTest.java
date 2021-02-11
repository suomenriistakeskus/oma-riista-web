package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.MergedRhyMappingTestHelper;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.F;
import fi.riista.util.NumberGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.MergedRhyMapping.RhyMerge.create;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.assertOrganisationTransformation;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.export;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.MOOSELIKE_TAXATION_PLANNING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.RHY_MEMBERS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SRVA_ALL_MOOSELIKE_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_OTHER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUM_OF_LUKE_CALCULATIONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.TOTAL_LUKE_CARNIVORE_PERSONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.getSubsidyCriteria;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.DateUtil.currentYear;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class RhySubsidyMergeResolverTest
        implements DefaultEntitySupplierProvider, RhyAnnualStatisticsTestDataPopulator {

    private RiistakeskuksenAlue rka;

    private int subsidyYear;

    private Riistanhoitoyhdistys unchangedRhy;
    private Riistanhoitoyhdistys oldRhy1;
    private Riistanhoitoyhdistys oldRhy2;
    private Riistanhoitoyhdistys newRhy; // merged from oldRhy1 and oldRhy2

    @Before
    public void setup() {
        rka = createRka("050");

        subsidyYear = currentYear();
        unchangedRhy = createRhy("051", rka);
        oldRhy1 = createRhy("111", rka);
        oldRhy2 = createRhy("222", rka);
        newRhy = createRhy("333", rka);

        MergedRhyMappingTestHelper.assignMerges(asList(
                create(subsidyYear, "111", "333"),
                create(subsidyYear, "222", "333")));
    }

    @After
    public void tearDown() {
        MergedRhyMappingTestHelper.reset();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return getEntitySupplier().getNumberGenerator();
    }

    @Test
    public void testMergeStatistics() {

        final RhyAnnualStatistics stats1 = createRhyAnnualStatistics(unchangedRhy);
        final RhyAnnualStatistics stats2 = createRhyAnnualStatistics(oldRhy1);
        final RhyAnnualStatistics stats3 = createRhyAnnualStatistics(oldRhy2);

        populateWithMatchingSubsidyTotalQuantities(stats1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(stats2, 1_000, 2, 40, 2, 4, 10, 4, 2, 1, 50, 1_000);
        populateWithMatchingSubsidyTotalQuantities(stats3, 1_000, 2, 40, 2, 4, 10, 4, 2, 2, 50, 1_000);

        final AnnualStatisticsExportDTO inputStats1 = export(stats1);
        final AnnualStatisticsExportDTO inputStats2 = export(stats2);
        final AnnualStatisticsExportDTO inputStats3 = export(stats3);

        final List<AnnualStatisticsExportDTO> mergedStatistics = createResolver(subsidyYear)
                .mergeStatistics(asList(inputStats1, inputStats2, inputStats3));

        assertThat(mergedStatistics, hasSize(2));

        final AnnualStatisticsExportDTO outputStats1 = mergedStatistics.get(0);
        assertOrganisationTransformation(unchangedRhy, outputStats1.getOrganisation());
        assertOrganisationTransformation(rka, outputStats1.getParentOrganisation());
        assertCalculatedSubsidyQuantities(outputStats1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);

        final AnnualStatisticsExportDTO outputStats2 = mergedStatistics.get(1);
        assertOrganisationTransformation(newRhy, outputStats2.getOrganisation());
        assertOrganisationTransformation(rka, outputStats2.getParentOrganisation());
        assertCalculatedSubsidyQuantities(outputStats2, 2_000, 4, 80, 4, 8, 20, 8, 4, 3, 100, 2_000);
    }

    private static void assertCalculatedSubsidyQuantities(final AnnualStatisticsExportDTO statistics,
                                                          final Integer expectedRhyMembers,
                                                          final Integer expectedHunterExamTrainingEvents,
                                                          final Integer expectedOtherTrainingEvents,
                                                          final Integer expectedStudentAndYouthTrainingEvents,
                                                          final Integer expectedHuntingControlEvents,
                                                          final Integer expectedSumOfLukeCalculations,
                                                          final Integer expectedLukeCarnivoreContactPersons,
                                                          final Integer expectedMooselikeTaxationPlanningEvents,
                                                          final Integer expectedWolfTerritoryWorkgroups,
                                                          final Integer expectedSrvaMooselikeEvents,
                                                          final Integer expectedSoldMhLicenses) {

        final ImmutableMap<SubsidyAllocationCriterion, Integer> assertPairs = ImmutableMap
                .<SubsidyAllocationCriterion, Integer>builder()
                .put(RHY_MEMBERS, expectedRhyMembers)
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, expectedHunterExamTrainingEvents)
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, expectedOtherTrainingEvents)
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, expectedStudentAndYouthTrainingEvents)
                .put(HUNTING_CONTROL_EVENTS, expectedHuntingControlEvents)
                .put(SUM_OF_LUKE_CALCULATIONS, expectedSumOfLukeCalculations)
                .put(TOTAL_LUKE_CARNIVORE_PERSONS, expectedLukeCarnivoreContactPersons)
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, expectedMooselikeTaxationPlanningEvents)
                .put(WOLF_TERRITORY_WORKGROUPS, expectedWolfTerritoryWorkgroups)
                .put(SRVA_ALL_MOOSELIKE_EVENTS, expectedSrvaMooselikeEvents)
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, expectedSoldMhLicenses)
                .build();

        assertPairs.forEach((criterion, expectedQuantity) -> {
            assertThat(
                    criterion.getRelatedStatisticItem().extractInteger(statistics),
                    equalTo(expectedQuantity),
                    criterion.name() + ": ");
        });
    }

    private void assertStatistics(final AnnualStatisticsExportDTO stats, final AnnualStatisticsExportDTO expectedStats) {
        getSubsidyCriteria(subsidyYear).forEach((criterion) -> {
            final AnnualStatisticItem statisticItem = criterion.getRelatedStatisticItem();

            assertThat(
                    statisticItem.extractInteger(stats),
                    equalTo(statisticItem.extractInteger(expectedStats)),
                    criterion.name() + ": ");
        });
    }

    @Test
    public void testMergePreviouslyGrantedSubsidies() {
        final Map<String, BigDecimal> rhyCodeToSubsidyAmountGrantedLastYear = ImmutableMap.of(
                unchangedRhy.getOfficialCode(), currency(101),
                oldRhy1.getOfficialCode(), currency(102),
                oldRhy2.getOfficialCode(), currency(103));

        final Map<String, BigDecimal> rhyCodeToSubsidyAmountGrantedInFirstBatchOfCurrentYear = ImmutableMap.of(
                unchangedRhy.getOfficialCode(), currency(51),
                newRhy.getOfficialCode(), currency(52));

        final PreviouslyGrantedSubsidiesDTO input = new PreviouslyGrantedSubsidiesDTO(
                rhyCodeToSubsidyAmountGrantedLastYear, rhyCodeToSubsidyAmountGrantedInFirstBatchOfCurrentYear);

        final PreviouslyGrantedSubsidiesDTO output =
                createResolver(subsidyYear).mergePreviouslyGrantedSubsidies(input);

        // Should remain unchanged
        assertThat(output.getRhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear(),
                equalTo(rhyCodeToSubsidyAmountGrantedInFirstBatchOfCurrentYear));

        final Map<String, BigDecimal> expectedMergedSubsidyAmountGrantedLastYear = ImmutableMap.of(
                unchangedRhy.getOfficialCode(), currency(101),
                newRhy.getOfficialCode(), currency(102 + 103));

        assertThat(output.getRhyCodeToSubsidyGrantedLastYear(), equalTo(expectedMergedSubsidyAmountGrantedLastYear));
    }

    @Test
    public void testCombineStatistics_unchangedRhy() {
        final RhyAnnualStatistics stats1 = createRhyAnnualStatistics(unchangedRhy);
        final RhyAnnualStatistics stats2 = createRhyAnnualStatistics(unchangedRhy);

        populateWithMatchingSubsidyTotalQuantities(stats1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(stats2, 1_000, 2, 40, 2, 4, 10, 4, 2, 1, 50, 1_000);

        final AnnualStatisticsExportDTO inputStats1 = export(stats1);
        final AnnualStatisticsExportDTO inputStats2 = export(stats2);

        final List<AnnualStatisticsExportDTO> mergedStatistics = createResolver(subsidyYear)
                .combine(asList(inputStats1), asList(inputStats2));
        assertThat(mergedStatistics, hasSize(1));

        final Integer expectedRhyMembers = 500 + 1_000;
        final Integer expectedHunterExamTrainingEvents = 1 + 2;
        final Integer expectedOtherTrainingEvents = 20 + 40;
        final Integer expectedStudentAndYouthTrainingEvents = 1 + 2;
        final Integer expectedHuntingControlEvents = 2 + 4;
        final Integer expectedSumOfLukeCalculations = 5 + 10;
        final Integer expectedLukeCarnivoreContactPersons = 2 + 4;
        final Integer expectedMooselikeTaxationPlanningEvents = 1 + 2;
        final Integer expectedWolfTerritoryWorkgroups = 1 + 1;
        final Integer expectedSrvaMooselikeEvents = 50 + 50;
        final Integer expectedSoldMhLicenses = 500 + 1_000;

        assertCalculatedSubsidyQuantities(mergedStatistics.get(0), expectedRhyMembers, expectedHunterExamTrainingEvents,
                expectedOtherTrainingEvents, expectedStudentAndYouthTrainingEvents, expectedHuntingControlEvents,
                expectedSumOfLukeCalculations, expectedLukeCarnivoreContactPersons, expectedMooselikeTaxationPlanningEvents,
                expectedWolfTerritoryWorkgroups, expectedSrvaMooselikeEvents, expectedSoldMhLicenses);

    }

    @Test
    public void testCombineStatistics_mergedRhy() {
        final RhyAnnualStatistics stats1 = createRhyAnnualStatistics(oldRhy1);
        final RhyAnnualStatistics stats2 = createRhyAnnualStatistics(newRhy);

        populateWithMatchingSubsidyTotalQuantities(stats1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(stats2, 1_000, 2, 40, 2, 4, 10, 4, 2, 1, 50, 1_000);

        final AnnualStatisticsExportDTO inputStats1 = export(stats1);
        final AnnualStatisticsExportDTO inputStats2 = export(stats2);

        final List<AnnualStatisticsExportDTO> mergedStatistics = createResolver(subsidyYear)
                .combine(asList(inputStats1), asList(inputStats2));
        assertThat(mergedStatistics, hasSize(2));

        final Map<String, AnnualStatisticsExportDTO> dtosByOfficialCode = F.index(mergedStatistics, dto -> dto.getOrganisation().getOfficialCode());

        final AnnualStatisticsExportDTO oldRhyDto = dtosByOfficialCode.get(oldRhy1.getOfficialCode());
        final AnnualStatisticsExportDTO newRhyDto = dtosByOfficialCode.get(newRhy.getOfficialCode());

        assertStatistics(oldRhyDto, inputStats1);
        assertStatistics(newRhyDto, inputStats2);

    }

    private RhySubsidyMergeResolver createResolver(final int subsidyYear) {
        return RhySubsidyTestHelper.createRhySubsidyMergeResolver(subsidyYear, unchangedRhy, oldRhy1, oldRhy2, newRhy);
    }

    private RiistakeskuksenAlue createRka(final String officialCode) {
        final RiistakeskuksenAlue rka = getEntitySupplier().newRiistakeskuksenAlue(officialCode);
        rka.setId(Long.parseLong(officialCode));
        return rka;
    }

    private Riistanhoitoyhdistys createRhy(final String officialCode, final RiistakeskuksenAlue rka) {
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys(rka, officialCode);
        rhy.setId(Long.parseLong(officialCode));
        return rhy;
    }

    private RhyAnnualStatistics createRhyAnnualStatistics(final Riistanhoitoyhdistys rhy) {
        return getEntitySupplier().newRhyAnnualStatistics(rhy);
    }
}
