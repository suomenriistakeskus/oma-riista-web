package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage4Calculation.calculateCompensation;
import static fi.riista.test.TestUtils.bd;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationStage4CalculationTest {

    private static OrganisationNameDTO rka;

    @BeforeClass
    public static void setUp() {
        rka = new OrganisationNameDTO();
        rka.setOfficialCode("000");
    }

    @Test
    public void testCalculateCompensation_whenCompensationNeeded() {
        final List<RhySubsidyStage3DTO> precalculatedInputs = asList(
                newInputAllocation("001", currency(11), 1, currency(8)),
                newInputAllocation("002", currency(6), 0, currency(6)),
                newInputAllocation("003", currency(20), 1, currency(12)),
                newInputAllocation("004", currency(10), 0, currency(12)),
                newInputAllocation("005", currency(3), 1, currency(6)),
                newInputAllocation("006", currency(10), 0, currency(22)));

        final SubsidyAllocationStage4ResultDTO result = calculateCompensation(precalculatedInputs);

        final List<RhySubsidyStage4DTO> resultAllocations = result.getRhyAllocations();
        assertEquals(6, resultAllocations.size());

        for (int i = 0; i < 6; i++) {
            assertCopiedProperties(resultAllocations.get(i), precalculatedInputs.get(i));
        }

        // Assertion done against pre-calculated values.
        assertCompensationResult(resultAllocations.get(0), bd("7.79"), currency(7), 0);
        assertCompensationResult(resultAllocations.get(1), bd("5.00"), currency(5), 0);
        assertCompensationResult(resultAllocations.get(2), bd("14.18"), currency(15), 1);
        assertCompensationResult(resultAllocations.get(3), bd("10.00"), currency(10), 0);
        assertCompensationResult(resultAllocations.get(4), bd("5.00"), currency(5), 0);
        assertCompensationResult(resultAllocations.get(5), bd("18.00"), currency(18), 0);
    }

    @Test
    public void testCalculateCompensation_whenCompensationNotNeeded() {
        // Contains a subset of inputs appearing in previous test.
        final List<RhySubsidyStage3DTO> precalculatedInputs = asList(
                newInputAllocation("001", currency(20), 0, currency(10)),
                newInputAllocation("002", currency(15), 0, currency(15)),
                newInputAllocation("003", currency(10), 1, currency(12)));

        final SubsidyAllocationStage4ResultDTO result = calculateCompensation(precalculatedInputs);

        final List<RhySubsidyStage4DTO> resultAllocations = result.getRhyAllocations();
        assertEquals(3, resultAllocations.size());

        for (int i = 0; i < 3; i++) {
            assertCopiedProperties(resultAllocations.get(i), precalculatedInputs.get(i));
        }

        // Monetary sums should remain unchanged because compensation is not expected to take place.
        assertCompensationResult(resultAllocations.get(0), currency(20), currency(20), 0);
        assertCompensationResult(resultAllocations.get(1), currency(15), currency(15), 0);
        assertCompensationResult(resultAllocations.get(2), currency(10), currency(10), 0);
    }

    private static void assertCopiedProperties(final RhySubsidyStage4DTO output, final RhySubsidyStage3DTO input) {
        assertEquals(input.getOrganisationInfo(), output.getOrganisationInfo());

        final SubsidyCalculationStage3DTO inputCalc = input.getCalculation();
        final SubsidyCalculationStage4DTO outputCalc = output.getCalculation();

        assertEquals(inputCalc.getCalculatedShares(), outputCalc.getCalculatedShares());
        assertEquals(inputCalc.getRemainderEurosGivenInStage2(), outputCalc.getRemainderEurosGivenInStage2());
        assertEquals(inputCalc.getSubsidyComparisonToLastYear(), outputCalc.getSubsidyComparisonToLastYear());
    }

    private static void assertCompensationResult(final RhySubsidyStage4DTO output,
                                                 final BigDecimal calculatedSubsidyAfterCompensation,
                                                 final BigDecimal expectedSubsidyAfterFinalRounding,
                                                 final int expectedRemainderEurosGivenInStage4) {

        final SubsidyRoundingDTO rounding = output.getCalculation().getStage4Rounding();

        assertEquals(calculatedSubsidyAfterCompensation, rounding.getSubsidyBeforeRounding());
        assertEquals(expectedSubsidyAfterFinalRounding, rounding.getSubsidyAfterRounding());
        assertEquals(expectedRemainderEurosGivenInStage4, rounding.getGivenRemainderEuros());
    }

    private static RhySubsidyStage3DTO newInputAllocation(final String rhyCode,
                                                          final BigDecimal subsidyAfterStage2RemainderAllocation,
                                                          final int remainderEurosGivenInStage2,
                                                          final BigDecimal subsidyGrantedLastYear) {

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        final SubsidyComparisonToLastYearDTO subsidyComparisonToLastYear =
                SubsidyComparisonToLastYearDTO.create(subsidyAfterStage2RemainderAllocation, subsidyGrantedLastYear);

        return new RhySubsidyStage3DTO(
                new RhyAndRkaDTO(rhy, rka),
                new SubsidyCalculationStage3DTO(
                        createEmptyCalculatedShares(),         // not relevant in tests of stage 4
                        subsidyAfterStage2RemainderAllocation,
                        remainderEurosGivenInStage2,
                        subsidyComparisonToLastYear));
    }
}
