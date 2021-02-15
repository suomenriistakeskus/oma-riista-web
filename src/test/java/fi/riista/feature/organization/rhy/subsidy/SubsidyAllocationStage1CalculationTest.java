package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.NumberGenerator;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.assertEquality;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.assertOrganisationTransformation;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.export;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_SUBSIDY_YEAR;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage1Calculation.calculateStatisticsBasedSubsidyAllocation;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationToCriteriaCalculation.calculateAllocationOfRhySubsidyToEachCriterion;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationStage1CalculationTest
        implements DefaultEntitySupplierProvider, RhyAnnualStatisticsTestDataPopulator {

    @Override
    public NumberGenerator getNumberGenerator() {
        return getEntitySupplier().getNumberGenerator();
    }

    @Test
    public void testCalculateStatisticsBasedSubsidyAllocation_smoke() {
        final RiistakeskuksenAlue rka = newRka("050");

        final Riistanhoitoyhdistys rhy1 = newRhy("051", rka);
        final Riistanhoitoyhdistys rhy2 = newRhy("052", rka);

        final RhyAnnualStatistics stats1 = newRhyAnnualStatistics(rhy1);
        final RhyAnnualStatistics stats2 = newRhyAnnualStatistics(rhy2);

        populateWithMatchingSubsidyTotalQuantities(stats1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(stats2, 2_000, 4, 80, 4, 8, 20, 8, 4, 3, 100, 2_000);

        final AnnualStatisticsExportDTO inputStats1 = export(stats1);
        final AnnualStatisticsExportDTO inputStats2 = export(stats2);

        final List<AnnualStatisticsExportDTO> allRhyStatistics = asList(inputStats1, inputStats2);

        final BigDecimal totalAllocatableSubsidyAmount = currency(1_000_000);
        final int subsidyYear = FIRST_SUBSIDY_YEAR;

        final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations =
                calculateAllocationOfRhySubsidyToEachCriterion(
                        allRhyStatistics, totalAllocatableSubsidyAmount, subsidyYear);

        final List<RhySubsidyStage1DTO> result =
                calculateStatisticsBasedSubsidyAllocation(subsidyYear, allRhyStatistics, criteriaSpecificAllocations);

        assertEquals(2, result.size());

        final RhySubsidyStage1DTO allocation1 = result.get(0);

        assertOrganisationTransformation(rhy1, allocation1.getOrganisationInfo().getRhy());
        assertOrganisationTransformation(rka, allocation1.getOrganisationInfo().getRka());

        final StatisticsBasedSubsidyShareDTO expectedShares1 = StatisticsBasedSubsidyShareDTO
                .builder()
                .withRhyMembers(500, bd("70000.00"))
                .withHunterExamTrainingEvents(1, bd("25000.00"))
                .withOtherTrainingEvents(20, bd("20000.00"))
                .withStudentAndYouthTrainingEvents(1, bd("20000.00"))
                .withHuntingControlEvents(2, bd("10000.00"))
                .withSumOfLukeCalculations(5, bd("15000.00"))
                .withLukeCarnivoreContactPersons(2, bd("5000.00"))
                .withMooselikeTaxationPlanningEvents(1, bd("10000.00"))
                .withWolfTerritoryWorkgroups(1, bd("12500.00"))
                .withSrvaMooselikeEvents(50, bd("16666.67"))
                .withSoldMhLicenses(500, bd("5000.00"))
                .build();

        assertEquality(subsidyYear, expectedShares1, allocation1.getCalculation().getCalculatedShares());
        assertEquals(currency(209_166), allocation1.getCalculation().getTotalRoundedShare());

        final RhySubsidyStage1DTO allocation2 = result.get(1);

        assertOrganisationTransformation(rhy2, allocation2.getOrganisationInfo().getRhy());
        assertOrganisationTransformation(rka, allocation2.getOrganisationInfo().getRka());

        final StatisticsBasedSubsidyShareDTO expectedShares2 = StatisticsBasedSubsidyShareDTO
                .builder()
                .withRhyMembers(2_000, bd("280000.00"))
                .withHunterExamTrainingEvents(4, bd("100000.00"))
                .withOtherTrainingEvents(80, bd("80000.00"))
                .withStudentAndYouthTrainingEvents(4, bd("80000.00"))
                .withHuntingControlEvents(8, bd("40000.00"))
                .withSumOfLukeCalculations(20, bd("60000.00"))
                .withLukeCarnivoreContactPersons(8, bd("20000.00"))
                .withMooselikeTaxationPlanningEvents(4, bd("40000.00"))
                .withWolfTerritoryWorkgroups(3, bd("37500.00"))
                .withSrvaMooselikeEvents(100, bd("33333.33"))
                .withSoldMhLicenses(2_000, bd("20000.00"))
                .build();

        assertEquality(subsidyYear, expectedShares2, allocation2.getCalculation().getCalculatedShares());
        assertEquals(currency(790_833), allocation2.getCalculation().getTotalRoundedShare());
    }

    private RiistakeskuksenAlue newRka(final String officialCode) {
        final RiistakeskuksenAlue rka = getEntitySupplier().newRiistakeskuksenAlue(officialCode);
        rka.setId(Long.parseLong(officialCode));
        return rka;
    }

    private Riistanhoitoyhdistys newRhy(final String officialCode, final RiistakeskuksenAlue rka) {
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys(rka, officialCode);
        rhy.setId(Long.parseLong(officialCode));
        return rhy;
    }

    private RhyAnnualStatistics newRhyAnnualStatistics(final Riistanhoitoyhdistys rhy) {
        return getEntitySupplier().newRhyAnnualStatistics(rhy);
    }
}
