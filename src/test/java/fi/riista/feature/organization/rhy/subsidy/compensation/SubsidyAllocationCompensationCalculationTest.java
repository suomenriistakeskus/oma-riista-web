package fi.riista.feature.organization.rhy.subsidy.compensation;

import com.google.common.collect.ImmutableList;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationCalculation.executeCompensationIfNeeded;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubsidyAllocationCompensationCalculationTest {

    @Test
    public void testExecuteCompensationIfNeeded_whenCompensationNeeded() {
        final List<SubsidyCompensationInputDTO> inputs = asList(
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_3,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT_2);

        final SubsidyAllocationCompensationResultDTO result = executeCompensationIfNeeded(inputs);

        final List<SubsidyAllocationCompensationRoundDTO> rounds = result.getRounds();
        assertEquals(2, rounds.size());

        final SubsidyAllocationCompensationRoundDTO round1 = rounds.get(0);

        final SubsidyAllocationCompensationBasis expectedBasis1 =
                new SubsidyAllocationCompensationBasis(currency(2 + 8), currency(11 + 6 + 20), bd("0.2702702703"));

        assertEquals(expectedBasis1, round1.getBasis());
        assertTrue(round1.isAnotherRoundNeeded());

        final List<Tuple3<String, BigDecimal, BigDecimal>> expectedOutputs1 = ImmutableList.of(
                Tuple.of("001", bd("8.02"), bd("2.98")),
                Tuple.of("002", bd("4.37"), bd("1.63")),
                Tuple.of("003", bd("14.59"), bd("5.41")),
                Tuple.of("004", bd("10.00"), null),
                Tuple.of("005", bd("5.00"), null),
                Tuple.of("006", bd("18.00"), null));

        assertEquals(expectedOutputs1, transformOutputToTuples(round1));

        final SubsidyAllocationCompensationRoundDTO round2 = rounds.get(1);

        final SubsidyAllocationCompensationBasis expectedBasis2 =
                new SubsidyAllocationCompensationBasis(bd("0.63"), bd("22.61"), bd("0.0278637771"));

        assertEquals(expectedBasis2, round2.getBasis());
        assertFalse(round2.isAnotherRoundNeeded());

        final List<Tuple3<String, BigDecimal, BigDecimal>> expectedOutputs2 = ImmutableList.of(
                Tuple.of("001", bd("7.79"), bd("0.23")),
                Tuple.of("002", bd("5.00"), null),
                Tuple.of("003", bd("14.18"), bd("0.41")),
                Tuple.of("004", bd("10.00"), null),
                Tuple.of("005", bd("5.00"), null),
                Tuple.of("006", bd("18.00"), null));

        assertEquals(expectedOutputs2, transformOutputToTuples(round2));
    }

    @Test
    public void testExecuteCompensationIfNeeded_whenCompensatioNotNeeded() {
        final List<SubsidyCompensationInputDTO> inputs = asList(
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_3,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT);

        final SubsidyAllocationCompensationResultDTO result = executeCompensationIfNeeded(inputs);

        assertTrue(result.getRounds().isEmpty());
        assertEquals(0, result.getNumberOfRounds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteCompensationIfNeeded_alreadyCompensatedNotAccepted() {
        executeCompensationIfNeeded(asList(SubsidyCompensationInputs.ALREADY_COMPENSATED));
    }

    private static List<Tuple3<String, BigDecimal, BigDecimal>> transformOutputToTuples(
            final SubsidyAllocationCompensationRoundDTO round) {

        return F.mapNonNullsToList(round.getResultingSubsidies(), output -> {

            return Tuple.of(output.getRhyCode(), output.getTotalSubsidyAfterCompensation(), output.getDecrement());
        });
    }
}
