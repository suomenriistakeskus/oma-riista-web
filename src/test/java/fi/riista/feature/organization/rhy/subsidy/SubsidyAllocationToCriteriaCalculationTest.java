package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

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
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationToCriteriaCalculation.calculateSubsidyAmountForEachCriterion;
import static fi.riista.test.TestUtils.bd;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationToCriteriaCalculationTest {

    @Test
    public void testCalculateSubsidyAmountForEachCriterion() {
        streamSubsidyYears().forEach(this::testCalculateSubsidyAmountForEachCriterion);
    }

    private void testCalculateSubsidyAmountForEachCriterion(final int subsidyYear) {
        final ImmutableMap.Builder<SubsidyAllocationCriterion, BigDecimal> builder = ImmutableMap
                .<SubsidyAllocationCriterion, BigDecimal>builder()
                .put(RHY_MEMBERS, bd("350000.000"))
                .put(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, bd("125000.000"))
                .put(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, bd("100000.000"))
                .put(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, bd("100000.000"))
                .put(HUNTING_CONTROL_EVENTS, bd("50000.000"))
                .put(MOOSELIKE_TAXATION_PLANNING_EVENTS, bd("50000.000"))
                .put(SRVA_ALL_MOOSELIKE_EVENTS, bd("50000.000"))
                .put(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, bd("25000.000"));

        if (subsidyYear >= 2021) {
            builder.put(SUM_OF_LUKE_CALCULATIONS, bd("75000.000"))
                    .put(TOTAL_LUKE_CARNIVORE_PERSONS, bd("40000.000"))
                    .put(WOLF_TERRITORY_WORKGROUPS, bd("35000.000"));
        } else {
            builder.put(SUM_OF_LUKE_CALCULATIONS_PRE_2021, bd("75000.000"))
                    .put(TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021, bd("25000.000"))
                    .put(WOLF_TERRITORY_WORKGROUPS_PRE_2021, bd("50000.000"));
        }

        final Map<SubsidyAllocationCriterion, BigDecimal> expected = builder.build();

        assertEquals(
                "Mismatching allocations for criteria for year " + subsidyYear + ", ",
                expected,
                calculateSubsidyAmountForEachCriterion(new BigDecimal(1_000_000), subsidyYear));
    }
}
