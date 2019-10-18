package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.NumberGenerator;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_ETELÄ_SOISALO_078;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_HEINÄVESI_056;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_KANGASLAMMI_060;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.assertOrganisationTransformation;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.export;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_SUBSIDY_YEAR;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.LUKE_CARNIVORE_CONTACT_PERSONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.MOOSELIKE_TAXATION_PLANNING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.RHY_MEMBERS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SRVA_ALL_MOOSELIKE_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_OTHER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUM_OF_LUKE_CALCULATIONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class RhySubsidyMergeResolverTest
        implements DefaultEntitySupplierProvider, RhyAnnualStatisticsTestDataPopulator {

    private RiistakeskuksenAlue rka;

    private Riistanhoitoyhdistys unchangedRhy;
    private Riistanhoitoyhdistys oldRhy1;
    private Riistanhoitoyhdistys oldRhy2;
    private Riistanhoitoyhdistys newRhy; // merged from oldRhy1 and oldRhy2

    @Before
    public void setup() {
        rka = createRka("050");

        unchangedRhy = createRhy("051", rka);
        oldRhy1 = createRhy(OLD_HEINÄVESI_056, rka);
        oldRhy2 = createRhy(OLD_KANGASLAMMI_060, rka);
        newRhy = createRhy(NEW_ETELÄ_SOISALO_078, rka);
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

        final List<AnnualStatisticsExportDTO> mergedStatistics = createResolver(FIRST_SUBSIDY_YEAR)
                .mergeStatistics(asList(inputStats1, inputStats2, inputStats3));

        assertEquals(2, mergedStatistics.size());

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
                .<SubsidyAllocationCriterion, Integer> builder()
                .put(RHY_MEMBERS, expectedRhyMembers)
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, expectedHunterExamTrainingEvents)
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, expectedOtherTrainingEvents)
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, expectedStudentAndYouthTrainingEvents)
                .put(HUNTING_CONTROL_EVENTS, expectedHuntingControlEvents)
                .put(SUM_OF_LUKE_CALCULATIONS, expectedSumOfLukeCalculations)
                .put(LUKE_CARNIVORE_CONTACT_PERSONS, expectedLukeCarnivoreContactPersons)
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, expectedMooselikeTaxationPlanningEvents)
                .put(WOLF_TERRITORY_WORKGROUPS, expectedWolfTerritoryWorkgroups)
                .put(SRVA_ALL_MOOSELIKE_EVENTS, expectedSrvaMooselikeEvents)
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, expectedSoldMhLicenses)
                .build();

        assertPairs.forEach((criterion, expectedQuantity) -> {
            assertEquals(
                    criterion.name() + ": ",
                    expectedQuantity,
                    criterion.getRelatedStatisticItem().extractInteger(statistics));
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
                createResolver(FIRST_SUBSIDY_YEAR).mergePreviouslyGrantedSubsidies(input);

        // Should remain unchanged
        assertEquals(
                rhyCodeToSubsidyAmountGrantedInFirstBatchOfCurrentYear,
                output.getRhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear());

        final Map<String, BigDecimal> expectedMergedSubsidyAmountGrantedLastYear = ImmutableMap.of(
                unchangedRhy.getOfficialCode(), currency(101),
                newRhy.getOfficialCode(), currency(102 + 103));

        assertEquals(expectedMergedSubsidyAmountGrantedLastYear, output.getRhyCodeToSubsidyGrantedLastYear());
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
