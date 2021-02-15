package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyRoundingDTO;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.feature.organization.rhy.subsidy2019.SubsidyAllocation2019Stage4Calculation.calculateCompensation;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocation2019Stage4CalculationTest {

    @Test
    public void testCalculateCompensation_whenCompensationNeeded() {
        final List<SubsidyAllocation2019Stage3DTO> precalculatedInputs = asList(
                newInputAllocation("001", currency(6), currency(5), currency(8), 1),
                newInputAllocation("002", currency(3), currency(3), currency(6), 0),
                newInputAllocation("003", currency(15), currency(5), currency(12), 1),
                newInputAllocation("004", currency(5), currency(5), currency(12), 0),
                newInputAllocation("005", currency(0), currency(3), currency(6), 1),
                newInputAllocation("006", currency(5), currency(5), currency(22), 0));

        final SubsidyAllocation2019ResultDTO result = calculateCompensation(precalculatedInputs);

        final List<SubsidyAllocation2019Stage4DTO> resultAllocations = result.getRhyAllocations();
        assertEquals(6, resultAllocations.size());

        for (int i = 0; i < 6; i++) {
            assertPropertiesNotExpectedToChange(resultAllocations.get(i), precalculatedInputs.get(i));
        }

        // Assertion done against pre-calculated values.
        assertCompensationResult(resultAllocations.get(0), bd("2.79"), currency(2), 0);
        assertCompensationResult(resultAllocations.get(1), bd("2.00"), currency(2), 0);
        assertCompensationResult(resultAllocations.get(2), bd("9.18"), currency(10), 1);
        assertCompensationResult(resultAllocations.get(3), bd("5.00"), currency(5), 0);
        assertCompensationResult(resultAllocations.get(4), bd("2.00"), currency(2), 0);
        assertCompensationResult(resultAllocations.get(5), bd("13.00"), currency(13), 0);
    }

    @Test
    public void testCalculateCompensation_whenCompensationNotNeeded() {
        // Contains a subset of inputs appearing in previous test.
        final List<SubsidyAllocation2019Stage3DTO> precalculatedInputs = asList(
                newInputAllocation("001", currency(6), currency(5), currency(8), 1),
                newInputAllocation("002", currency(3), currency(3), currency(6), 0),
                newInputAllocation("003", currency(15), currency(5), currency(12), 1),
                newInputAllocation("004", currency(5), currency(5), currency(12), 0));

        final SubsidyAllocation2019ResultDTO result = calculateCompensation(precalculatedInputs);

        final List<SubsidyAllocation2019Stage4DTO> resultAllocations = result.getRhyAllocations();
        assertEquals(4, resultAllocations.size());

        for (int i = 0; i < 4; i++) {
            assertPropertiesNotExpectedToChange(resultAllocations.get(i), precalculatedInputs.get(i));
        }

        // Monetary sums should remain unchanged because compensation is not expected to happen.
        assertCompensationResult(resultAllocations.get(0), currency(6), currency(6), 0);
        assertCompensationResult(resultAllocations.get(1), currency(3), currency(3), 0);
        assertCompensationResult(resultAllocations.get(2), currency(15), currency(15), 0);
        assertCompensationResult(resultAllocations.get(3), currency(5), currency(5), 0);
    }

    private static void assertPropertiesNotExpectedToChange(final SubsidyAllocation2019Stage4DTO output,
                                                            final SubsidyAllocation2019Stage3DTO input) {

        // Comparing by object identities here since these properties are expected to be copied
        // from input to output.
        assertEquals(input.getRhy(), output.getOrganisation());
        assertEquals(input.getRka(), output.getParentOrganisation());
        assertEquals(input.getCalculatedShares(), output.getCalculatedShares());
        assertEquals(input.getRemainderEurosGivenInStage2(), output.getRemainderEurosGivenInStage2());
        assertEquals(input.getSubsidyBatchInfo(), output.getSubsidyBatchInfo());
    }

    private static void assertCompensationResult(final SubsidyAllocation2019Stage4DTO output,
                                                 final BigDecimal expectedSubsidyBeforeFinalRounding,
                                                 final BigDecimal expectedSubsidyAfterFinalRounding,
                                                 final int expectedRemainderEurosGivenInStage4) {

        final SubsidyRoundingDTO rounding = output.getStage4Rounding();

        assertEquals(expectedSubsidyBeforeFinalRounding, rounding.getSubsidyBeforeRounding());
        assertEquals(expectedSubsidyAfterFinalRounding, rounding.getSubsidyAfterRounding());
        assertEquals(expectedRemainderEurosGivenInStage4, rounding.getGivenRemainderEuros());
    }

    private static SubsidyAllocation2019Stage3DTO newInputAllocation(final String rhyCode,
                                                                     final BigDecimal calculatedSubsidyBeforeCompensation,
                                                                     final BigDecimal subsidyGrantedInFirstBatch,
                                                                     final BigDecimal subsidyGrantedLastYear,
                                                                     final int remainderEurosGivenInStage2) {

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        final SubsidyBatch2019InfoDTO subsidyBatchInfo = SubsidyBatch2019InfoDTO
                .create(calculatedSubsidyBeforeCompensation, subsidyGrantedInFirstBatch, subsidyGrantedLastYear);

        return new SubsidyAllocation2019Stage3DTO(
                rhy,
                new OrganisationNameDTO(),           // not relevant in tests of stage 4
                createEmptyCalculatedShares(),       // not relevant in tests of stage 4
                calculatedSubsidyBeforeCompensation,
                remainderEurosGivenInStage2,
                subsidyBatchInfo);
    }
}
