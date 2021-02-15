package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;
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

public class SubsidyAllocation2019CompensationRoundTest {

    private final List<SubsidyCompensation2019InputDTO> inputs = createDefaultInput();

    private final SubsidyAllocationCompensationBasis expectedBasis =
            new SubsidyAllocationCompensationBasis(currency(10), currency(17), bd("0.5882352941"));

    @Test
    public void testRun() {
        final SubsidyAllocation2019CompensationRoundDTO round = SubsidyAllocation2019CompensationRound.run(inputs);
        final SubsidyAllocationCompensationBasis basis = round.getBasis();

        assertBasis(basis);

        final List<SubsidyCompensation2019OutputDTO> compensationOutputs = round.getResultingSubsidies();

        assertRhyCodes(getAllInputRhyCodes(), compensationOutputs);

        final Map<Boolean, List<SubsidyCompensation2019OutputDTO>> partitionByDownscaling = compensationOutputs
                .stream()
                .collect(partitioningBy(SubsidyCompensation2019OutputDTO::isDownscaled));

        final List<SubsidyCompensation2019OutputDTO> downscaled = partitionByDownscaling.get(true);
        final List<SubsidyCompensation2019OutputDTO> compensatedOrKeptUnchanged = partitionByDownscaling.get(false);

        assertRhyCodes(asList("001", "002"), downscaled);
        assertRhyCodes(asList("004", "005", "007", "008"), compensatedOrKeptUnchanged);

        assertDecrement(compensatedOrKeptUnchanged, output -> null);

        final Map<String, SubsidyCompensation2019InputDTO> inputIndex =
                F.index(inputs, SubsidyCompensation2019InputDTO::getRhyCode);

        assertDecrement(downscaled, output -> {

            final String rhyCode = output.getRhyCode();
            final SubsidyCompensation2019InputDTO input = inputIndex.get(rhyCode);
            final BigDecimal totalSubsidyBeforeCompensation = input.getTotalSubsidyCalculatedForCurrentYear();

            return calculateDecrement(totalSubsidyBeforeCompensation, basis.getDecrementCoefficient());
        });
    }

    private List<String> getAllInputRhyCodes() {
        return inputs.stream().map(SubsidyCompensation2019InputDTO::getRhyCode).sorted().collect(toList());
    }

    private void assertBasis(final SubsidyAllocationCompensationBasis actualBasis) {
        assertEquals(expectedBasis, actualBasis);
    }

    private static void assertRhyCodes(final List<String> expected,
                                       final List<SubsidyCompensation2019OutputDTO> outputs) {

        assertEquals(expected, extractRhyCodes(outputs));
    }

    private static void assertDecrement(final List<SubsidyCompensation2019OutputDTO> outputs,
                                        final Function<SubsidyCompensation2019OutputDTO, BigDecimal> getExpectedDecrement) {
        outputs.forEach(output -> {

            final BigDecimal expectedDecrement = getExpectedDecrement.apply(output);

            assertEquals("Failed for RHY code " + output.getRhyCode() + ", ", expectedDecrement, output.getDecrement());
        });
    }

    private static List<String> extractRhyCodes(final List<SubsidyCompensation2019OutputDTO> outputs) {
        return F.mapNonNullsToList(outputs, SubsidyCompensation2019OutputDTO::getRhyCode);
    }

    private static List<SubsidyCompensation2019InputDTO> createDefaultInput() {
        return asList(
                SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT,
                SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT_2,
                SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT,
                SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT,
                SubsidyCompensation2019Inputs.NEGATIVE_SECOND_BATCH,
                SubsidyCompensation2019Inputs.NEGATIVE_SECOND_BATCH_AND_TOTAL_SUBSIDY_BELOW_LOWER_LIMIT);
    }
}
