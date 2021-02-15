package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.NumberGenerator;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

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
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUM_OF_LUKE_CALCULATIONS_PRE_2021;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.TOTAL_LUKE_CARNIVORE_PERSONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS_PRE_2021;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationUnitAmountCalculation.calculateUnitAmountsForSubsidyCriteria;
import static fi.riista.test.TestUtils.bd;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationUnitAmountCalculationTest
        implements DefaultEntitySupplierProvider, RhyAnnualStatisticsTestDataPopulator {

    private RhyAnnualStatistics statistics1;
    private RhyAnnualStatistics statistics2;

    @Before
    public void setup() {
        final RiistakeskuksenAlue rka = newRka("050");

        statistics1 = getEntitySupplier().newRhyAnnualStatistics(newRhy("051", rka));
        statistics2 = getEntitySupplier().newRhyAnnualStatistics(newRhy("052", rka));
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return getEntitySupplier().getNumberGenerator();
    }

    private static final Map<SubsidyAllocationCriterion, BigDecimal> ALLOCATED_AMOUNT_FOR_CRITERIA = ImmutableMap
            .<SubsidyAllocationCriterion, BigDecimal> builder()
            .put(RHY_MEMBERS, bd("350000.000"))
            .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("125000.000"))
            .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("100000.000"))
            .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("100000.000"))
            .put(HUNTING_CONTROL_EVENTS, bd("50000.000"))
            .put(SUM_OF_LUKE_CALCULATIONS, bd("75000.000"))
            .put(TOTAL_LUKE_CARNIVORE_PERSONS, bd("40000.000"))
            .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("50000.000"))
            .put(WOLF_TERRITORY_WORKGROUPS, bd("35000.000"))
            .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("50000.000"))
            .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("25000.000"))
            .build();

    private static final Map<SubsidyAllocationCriterion, BigDecimal> ALLOCATED_AMOUNT_FOR_CRITERIA_PRE_2021 = ImmutableMap
            .<SubsidyAllocationCriterion, BigDecimal> builder()
            .put(RHY_MEMBERS, bd("350000.000"))
            .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("125000.000"))
            .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("100000.000"))
            .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("100000.000"))
            .put(HUNTING_CONTROL_EVENTS, bd("50000.000"))
            .put(SUM_OF_LUKE_CALCULATIONS_PRE_2021, bd("75000.000"))
            .put(TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021, bd("25000.000"))
            .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("50000.000"))
            .put(WOLF_TERRITORY_WORKGROUPS_PRE_2021, bd("50000.000"))
            .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("50000.000"))
            .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("25000.000"))
            .build();

    @Test
    public void testCalculateUnitAmountsForSubsidyCriteria() {
        populateWithMatchingSubsidyTotalQuantities(statistics1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(statistics2, 2000, 4, 80, 4, 8, 20, 8, 4, 3, 100, 2000);

        final Map<SubsidyAllocationCriterion, BigDecimal> expected = ImmutableMap
                .<SubsidyAllocationCriterion, BigDecimal> builder()
                .put(RHY_MEMBERS, bd("140.0000000000"))
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("25000.0000000000"))
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("1000.0000000000"))
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("20000.0000000000"))
                .put(HUNTING_CONTROL_EVENTS, bd("5000.0000000000"))
                .put(SUM_OF_LUKE_CALCULATIONS, bd("3000.0000000000"))
                .put(TOTAL_LUKE_CARNIVORE_PERSONS, bd("4000.0000000000"))
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("10000.0000000000"))
                .put(WOLF_TERRITORY_WORKGROUPS, bd("8750.0000000000"))
                .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("333.3333333333"))
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("10.0000000000"))
                .build();

        test(ALLOCATED_AMOUNT_FOR_CRITERIA, expected, statistics1, statistics2);
    }

    @Test
    public void testCalculateUnitAmountsForSubsidyCriteria_pre2021() {
        populateWithMatchingSubsidyTotalQuantities(statistics1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(statistics2, 2000, 4, 80, 4, 8, 20, 8, 4, 3, 100, 2000);

        final Map<SubsidyAllocationCriterion, BigDecimal> expected = ImmutableMap
                .<SubsidyAllocationCriterion, BigDecimal> builder()
                .put(RHY_MEMBERS, bd("140.0000000000"))
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("25000.0000000000"))
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("1000.0000000000"))
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("20000.0000000000"))
                .put(HUNTING_CONTROL_EVENTS, bd("5000.0000000000"))
                .put(SUM_OF_LUKE_CALCULATIONS_PRE_2021, bd("3000.0000000000"))
                .put(TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021, bd("2500.0000000000"))
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("10000.0000000000"))
                .put(WOLF_TERRITORY_WORKGROUPS_PRE_2021, bd("12500.0000000000"))
                .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("333.3333333333"))
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("10.0000000000"))
                .build();

        test(ALLOCATED_AMOUNT_FOR_CRITERIA_PRE_2021, expected, statistics1, statistics2);
    }

    @Test
    public void testCalculateUnitAmountsForSubsidyCriteria_withOneRhyHavingZeroQuantities() {
        populateWithMatchingSubsidyTotalQuantities(statistics1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(statistics2, 0);

        final Map<SubsidyAllocationCriterion, BigDecimal> expected = ImmutableMap
                .<SubsidyAllocationCriterion, BigDecimal> builder()
                .put(RHY_MEMBERS, bd("700.0000000000"))
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("125000.0000000000"))
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("5000.0000000000"))
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("100000.0000000000"))
                .put(HUNTING_CONTROL_EVENTS, bd("25000.0000000000"))
                .put(SUM_OF_LUKE_CALCULATIONS, bd("15000.0000000000"))
                .put(TOTAL_LUKE_CARNIVORE_PERSONS, bd("20000.0000000000"))
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("50000.0000000000"))
                .put(WOLF_TERRITORY_WORKGROUPS, bd("35000.0000000000"))
                .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("1000.0000000000"))
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("50.0000000000"))
                .build();

        test(ALLOCATED_AMOUNT_FOR_CRITERIA, expected, statistics1, statistics2);
    }

    @Test
    public void testCalculateUnitAmountsForSubsidyCriteria_withOneRhyHavingNullQuantities() {
        populateWithMatchingSubsidyTotalQuantities(statistics1, 500, 1, 20, 1, 2, 5, 2, 1, 1, 50, 500);
        populateWithMatchingSubsidyTotalQuantities(statistics2, null);

        final Map<SubsidyAllocationCriterion, BigDecimal> expected = ImmutableMap
                .<SubsidyAllocationCriterion, BigDecimal> builder()
                .put(RHY_MEMBERS, bd("700.0000000000"))
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("125000.0000000000"))
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("5000.0000000000"))
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("100000.0000000000"))
                .put(HUNTING_CONTROL_EVENTS, bd("25000.0000000000"))
                .put(SUM_OF_LUKE_CALCULATIONS, bd("15000.0000000000"))
                .put(TOTAL_LUKE_CARNIVORE_PERSONS, bd("20000.0000000000"))
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("50000.0000000000"))
                .put(WOLF_TERRITORY_WORKGROUPS, bd("35000.0000000000"))
                .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("1000.0000000000"))
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("50.0000000000"))
                .build();

        test(ALLOCATED_AMOUNT_FOR_CRITERIA, expected, statistics1, statistics2);
    }

    @Test(expected = IllegalStateException.class)
    public void testCalculateUnitAmountsForSubsidyCriteria_whenAllQuantitiesAreZero() {
        populateWithMatchingSubsidyTotalQuantities(statistics1, 0);
        populateWithMatchingSubsidyTotalQuantities(statistics2, 0);

        calculateUnitAmountsForSubsidyCriteria(export(statistics1, statistics2), ALLOCATED_AMOUNT_FOR_CRITERIA);
    }

    @Test(expected = IllegalStateException.class)
    public void testCalculateUnitAmountsForSubsidyCriteria_whenAllQuantitiesAreNull() {
        populateWithMatchingSubsidyTotalQuantities(statistics1, null);
        populateWithMatchingSubsidyTotalQuantities(statistics2, null);

        calculateUnitAmountsForSubsidyCriteria(export(statistics1, statistics2), ALLOCATED_AMOUNT_FOR_CRITERIA);
    }

    private static void test(final Map<SubsidyAllocationCriterion, BigDecimal> allocatedAmounts,
                             final Map<SubsidyAllocationCriterion, BigDecimal> expected,
                             final RhyAnnualStatistics... stats) {

        final Map<SubsidyAllocationCriterion, BigDecimal> actual =
                calculateUnitAmountsForSubsidyCriteria(export(stats), allocatedAmounts);

        final Set<SubsidyAllocationCriterion> expectedCriteria = expected.keySet();
        final Set<SubsidyAllocationCriterion> actualCriteria = actual.keySet();

        assertEquals("Mismatch between included criteria", expectedCriteria, actualCriteria);

        for (final SubsidyAllocationCriterion criterion : expectedCriteria) {
            assertEquals(expected.get(criterion), actual.get(criterion));
        }
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
}
