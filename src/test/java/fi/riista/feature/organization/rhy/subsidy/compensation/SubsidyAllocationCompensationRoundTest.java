package fi.riista.feature.organization.rhy.subsidy.compensation;

import fi.riista.util.F;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.calculateDecrement;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationCompensationRoundTest {

    private final List<SubsidyCompensationInputDTO> inputs = createDefaultInput();

    private final SubsidyAllocationCompensationBasis expectedBasis =
            new SubsidyAllocationCompensationBasis(currency(10), currency(17), bd("0.5882352941"));

    @Test
    public void testRun() {
        final SubsidyAllocationCompensationRoundDTO round = SubsidyAllocationCompensationRound.run(inputs);
        final SubsidyAllocationCompensationBasis basis = round.getBasis();

        assertBasis(basis);

        final List<SubsidyCompensationOutputDTO> compensationOutputs = round.getResultingSubsidies();

        assertRhyCodes(getAllInputRhyCodes(), compensationOutputs);

        final Map<Boolean, List<SubsidyCompensationOutputDTO>> partitionByDownscaling = compensationOutputs
                .stream()
                .collect(partitioningBy(SubsidyCompensationOutputDTO::isDownscaled));

        final List<SubsidyCompensationOutputDTO> downscaled = partitionByDownscaling.get(true);
        final List<SubsidyCompensationOutputDTO> compensatedOrKeptUnchanged = partitionByDownscaling.get(false);

        assertRhyCodes(asList("001", "002"), downscaled);
        assertRhyCodes(asList("004", "005", "006"), compensatedOrKeptUnchanged);

        assertDecrement(compensatedOrKeptUnchanged, output -> null);

        final Map<String, SubsidyCompensationInputDTO> inputIndex =
                F.index(inputs, SubsidyCompensationInputDTO::getRhyCode);

        assertDecrement(downscaled, output -> {

            final String rhyCode = output.getRhyCode();
            final SubsidyCompensationInputDTO input = inputIndex.get(rhyCode);
            final BigDecimal totalSubsidyBeforeCompensation = input.getCalculatedSubsidy();

            return calculateDecrement(totalSubsidyBeforeCompensation, basis.getDecrementCoefficient());
        });
    }

    private List<String> getAllInputRhyCodes() {
        return inputs.stream().map(SubsidyCompensationInputDTO::getRhyCode).sorted().collect(toList());
    }

    private void assertBasis(final SubsidyAllocationCompensationBasis actualBasis) {
        assertEquals(expectedBasis, actualBasis);
    }

    private static void assertRhyCodes(final List<String> expected, final List<SubsidyCompensationOutputDTO> outputs) {
        assertEquals(expected, extractRhyCodes(outputs));
    }

    private static void assertDecrement(final List<SubsidyCompensationOutputDTO> outputs,
                                        final Function<SubsidyCompensationOutputDTO, BigDecimal> getExpectedDecrement) {
        outputs.forEach(output -> {

            final BigDecimal expectedDecrement = getExpectedDecrement.apply(output);

            assertEquals("Failed for RHY code " + output.getRhyCode() + ", ", expectedDecrement, output.getDecrement());
        });
    }

    private static List<String> extractRhyCodes(final List<SubsidyCompensationOutputDTO> outputs) {
        return F.mapNonNullsToList(outputs, SubsidyCompensationOutputDTO::getRhyCode);
    }

    private static List<SubsidyCompensationInputDTO> createDefaultInput() {
        return asList(
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT,
                SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT_2);
    }
}
