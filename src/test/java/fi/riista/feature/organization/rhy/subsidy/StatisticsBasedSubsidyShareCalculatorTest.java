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
import java.util.HashMap;
import java.util.Map;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.assertEquality;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.export;
import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.streamSubsidyYears;
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
import static fi.riista.test.TestUtils.bd;
import static org.junit.Assert.assertEquals;

public class StatisticsBasedSubsidyShareCalculatorTest
        implements DefaultEntitySupplierProvider, RhyAnnualStatisticsTestDataPopulator {

    private RhyAnnualStatistics statistics;

    @Before
    public void setup() {
        final String rkaCode = "050";
        final RiistakeskuksenAlue rka = getEntitySupplier().newRiistakeskuksenAlue(rkaCode);
        rka.setId(Long.parseLong(rkaCode));

        final String rhyCode = "051";
        final Riistanhoitoyhdistys rhy = getEntitySupplier().newRiistanhoitoyhdistys(rka, rhyCode);
        rhy.setId(Long.parseLong(rhyCode));

        statistics = getEntitySupplier().newRhyAnnualStatistics(rhy);
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return getEntitySupplier().getNumberGenerator();
    }

    // Initialize test amount with fibonacci series values
    private static final Map<SubsidyAllocationCriterion, BigDecimal> UNIT_AMOUNTS_PRE_2021 = ImmutableMap
            .<SubsidyAllocationCriterion, BigDecimal>builder()
            .put(RHY_MEMBERS, bd(1))
            .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd(2))
            .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd(3))
            .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd(5))
            .put(HUNTING_CONTROL_EVENTS, bd(8))
            .put(SUM_OF_LUKE_CALCULATIONS_PRE_2021, bd(13))
            .put(TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021, bd(21))
            .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd(34))
            .put(WOLF_TERRITORY_WORKGROUPS_PRE_2021, bd(55))
            .put(SRVA_ALL_MOOSELIKE_EVENTS, bd(89))
            .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd(144))
            .build();

    private static final Map<SubsidyAllocationCriterion, BigDecimal> UNIT_AMOUNTS = ImmutableMap
            .<SubsidyAllocationCriterion, BigDecimal>builder()
            .put(RHY_MEMBERS, bd(1))
            .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd(2))
            .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd(3))
            .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd(5))
            .put(HUNTING_CONTROL_EVENTS, bd(8))
            .put(SUM_OF_LUKE_CALCULATIONS, bd(13))
            .put(TOTAL_LUKE_CARNIVORE_PERSONS, bd(21))
            .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd(34))
            .put(WOLF_TERRITORY_WORKGROUPS, bd(55))
            .put(SRVA_ALL_MOOSELIKE_EVENTS, bd(89))
            .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd(144))
            .build();

    private static Map<SubsidyAllocationCriterion, BigDecimal> selectUnitAmounts(final int subsidyYear) {
        return subsidyYear >= 2021 ? UNIT_AMOUNTS : UNIT_AMOUNTS_PRE_2021;
    }

    @Test
    public void testCalculateSubsidyShare_forScaleAndRounding() {
        streamSubsidyYears().forEach(subsidyYear -> {
            final StatisticsBasedSubsidyShareCalculator shareCalculator =
                    new StatisticsBasedSubsidyShareCalculator(ImmutableMap
                            .<SubsidyAllocationCriterion, BigDecimal>builder()
                            .put(RHY_MEMBERS, bd("1.1111111111"))
                            .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("2.2222222222"))
                            .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("3.3333333333"))
                            .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("5.5555555555"))
                            .put(HUNTING_CONTROL_EVENTS, bd("8.8888888888"))
                            .put(subsidyYear >= 2021 ? SUM_OF_LUKE_CALCULATIONS : SUM_OF_LUKE_CALCULATIONS_PRE_2021, bd(13))
                            .put(subsidyYear >= 2021 ? TOTAL_LUKE_CARNIVORE_PERSONS : TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021, bd(21))
                            .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd(34))
                            .put(subsidyYear >= 2021 ? WOLF_TERRITORY_WORKGROUPS : WOLF_TERRITORY_WORKGROUPS_PRE_2021, bd(55))
                            .put(SRVA_ALL_MOOSELIKE_EVENTS, bd(89))
                            .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd(144))
                            .build());

            populateWithMatchingSubsidyTotalQuantities(statistics, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31);

            final StatisticsBasedSubsidyShareDTO expected = StatisticsBasedSubsidyShareDTO
                    .builder()
                    .withRhyMembers(2, bd("2.22"))
                    .withHunterExamTrainingEvents(3, bd("6.67"))
                    .withOtherTrainingEvents(5, bd("16.67"))
                    .withStudentAndYouthTrainingEvents(7, bd("38.89"))
                    .withHuntingControlEvents(11, bd("97.78"))
                    .withSumOfLukeCalculations(13, bd("169.00"))
                    .withLukeCarnivoreContactPersons(17, bd("357.00"))
                    .withMooselikeTaxationPlanningEvents(19, bd("646.00"))
                    .withWolfTerritoryWorkgroups(23, bd("1265.00"))
                    .withSrvaMooselikeEvents(29, bd("2581.00"))
                    .withSoldMhLicenses(31, bd("4464.00"))
                    .build();

            final StatisticsBasedSubsidyShareDTO result =
                    shareCalculator.calculateSubsidyShare(subsidyYear, export(statistics));

            assertEquality(subsidyYear, expected, result);
            assertEquals(bd("9644.23"), result.countSumOfAllShares());
        });
    }

    @Test
    public void testCalculateSubsidyShare_withZeroQuantities() {
        streamSubsidyYears().forEach(subsidyYear -> {
            final StatisticsBasedSubsidyShareCalculator shareCalculator =
                    new StatisticsBasedSubsidyShareCalculator(selectUnitAmounts(subsidyYear));

            populateWithMatchingSubsidyTotalQuantities(statistics, 0);

            final StatisticsBasedSubsidyShareDTO expected = StatisticsBasedSubsidyShareDTO
                    .builder()
                    .withRhyMembers(0, ZERO_MONETARY_AMOUNT)
                    .withHunterExamTrainingEvents(0, ZERO_MONETARY_AMOUNT)
                    .withOtherTrainingEvents(0, ZERO_MONETARY_AMOUNT)
                    .withStudentAndYouthTrainingEvents(0, ZERO_MONETARY_AMOUNT)
                    .withHuntingControlEvents(0, ZERO_MONETARY_AMOUNT)
                    .withSumOfLukeCalculations(0, ZERO_MONETARY_AMOUNT)
                    .withLukeCarnivoreContactPersons(0, ZERO_MONETARY_AMOUNT)
                    .withMooselikeTaxationPlanningEvents(0, ZERO_MONETARY_AMOUNT)
                    .withWolfTerritoryWorkgroups(0, ZERO_MONETARY_AMOUNT)
                    .withSrvaMooselikeEvents(0, ZERO_MONETARY_AMOUNT)
                    .withSoldMhLicenses(0, ZERO_MONETARY_AMOUNT)
                    .build();

            final StatisticsBasedSubsidyShareDTO result = shareCalculator.calculateSubsidyShare(subsidyYear, export(statistics));

            assertEquality(subsidyYear, expected, result);
            assertEquals(ZERO_MONETARY_AMOUNT, result.countSumOfAllShares());
        });
    }

    @Test
    public void testCalculateSubsidyShare_withNullQuantities() {
        streamSubsidyYears().forEach(subsidyYear -> {

            final StatisticsBasedSubsidyShareCalculator shareCalculator =
                    new StatisticsBasedSubsidyShareCalculator(selectUnitAmounts(subsidyYear));

            populateWithMatchingSubsidyTotalQuantities(statistics, null);

            final StatisticsBasedSubsidyShareDTO expected = StatisticsBasedSubsidyShareDTO
                    .builder()
                    .withRhyMembers(null, ZERO_MONETARY_AMOUNT)
                    .withHunterExamTrainingEvents(null, ZERO_MONETARY_AMOUNT)
                    .withOtherTrainingEvents(null, ZERO_MONETARY_AMOUNT)
                    .withStudentAndYouthTrainingEvents(null, ZERO_MONETARY_AMOUNT)
                    .withHuntingControlEvents(null, ZERO_MONETARY_AMOUNT)
                    .withSumOfLukeCalculations(null, ZERO_MONETARY_AMOUNT)
                    .withLukeCarnivoreContactPersons(null, ZERO_MONETARY_AMOUNT)
                    .withMooselikeTaxationPlanningEvents(null, ZERO_MONETARY_AMOUNT)
                    .withWolfTerritoryWorkgroups(null, ZERO_MONETARY_AMOUNT)
                    .withSrvaMooselikeEvents(null, ZERO_MONETARY_AMOUNT)
                    .withSoldMhLicenses(null, ZERO_MONETARY_AMOUNT)
                    .build();

            final StatisticsBasedSubsidyShareDTO result = shareCalculator.calculateSubsidyShare(subsidyYear, export(statistics));

            assertEquality(subsidyYear, expected, result);
            assertEquals(ZERO_MONETARY_AMOUNT, result.countSumOfAllShares());
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testCalculateSubsidyShare_whenUnitAmountIsMissing() {
        streamSubsidyYears().forEach(subsidyYear -> {
            final Map<SubsidyAllocationCriterion, BigDecimal> unitAmounts =
                    new HashMap<>(selectUnitAmounts(subsidyYear));
            unitAmounts.remove(RHY_MEMBERS);

            populateWithMatchingSubsidyTotalQuantities(statistics, 1);

            new StatisticsBasedSubsidyShareCalculator(unitAmounts).calculateSubsidyShare(subsidyYear, export(statistics));
        });
    }
}
