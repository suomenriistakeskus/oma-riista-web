package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage2Calculation.calculateRemainder;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.NumberUtils.currencySum;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationStage2CalculationTest {

    private static final String RHY1_CODE = "001";
    private static final String RHY2_CODE = "002";
    private static final String RHY3_CODE = "003";
    private static final String RHY4_CODE = "004";

    private static OrganisationNameDTO rka;

    private List<RhySubsidyStage1DTO> inputAllocations;

    private BigDecimal subsidyAmountAllocatedInStage1;

    @Before
    public void setup() {
        rka = new OrganisationNameDTO();
        rka.setOfficialCode("000");

        inputAllocations = asList(
                newInputAllocation(RHY1_CODE, currency(100)),
                newInputAllocation(RHY2_CODE, currency(101)),
                newInputAllocation(RHY3_CODE, currency(102)),
                newInputAllocation(RHY4_CODE, currency(103)));

        subsidyAmountAllocatedInStage1 =
                currencySum(inputAllocations, dto -> dto.getCalculation().getTotalRoundedShare());
    }

    @Test
    public void testCalculateRemainder_whenThereAreLessRemainderEurosThanNumberOfRhys() {
        final int numRhys = inputAllocations.size();
        final BigDecimal totalSubsidyAmount = subsidyAmountAllocatedInStage1.add(currency(numRhys - 1));

        final List<RhySubsidyStage2DTO> output = calculateRemainder(totalSubsidyAmount, inputAllocations);

        final List<Tuple3<String, Integer, BigDecimal>> expectedOutput = ImmutableList.of(
                Tuple.of(RHY1_CODE, 1, currency(101)),
                Tuple.of(RHY2_CODE, 1, currency(102)),
                Tuple.of(RHY3_CODE, 1, currency(103)),
                Tuple.of(RHY4_CODE, 0, currency(103)));

        assertEquals(expectedOutput, tuplesFromOutput(output));
    }

    @Test
    public void testCalculateRemainder_whenThereAreMoreRemainderEurosThanNumberOfRhys() {
        final int numRhys = inputAllocations.size();
        final BigDecimal totalSubsidyAmount = subsidyAmountAllocatedInStage1.add(currency(numRhys + 1));

        final List<RhySubsidyStage2DTO> output = calculateRemainder(totalSubsidyAmount, inputAllocations);

        final List<Tuple3<String, Integer, BigDecimal>> expectedOutput = ImmutableList.of(
                Tuple.of(RHY1_CODE, 2, currency(102)),
                Tuple.of(RHY2_CODE, 1, currency(102)),
                Tuple.of(RHY3_CODE, 1, currency(103)),
                Tuple.of(RHY4_CODE, 1, currency(104)));

        assertEquals(expectedOutput, tuplesFromOutput(output));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateRemainder_withNegativeRemainder() {
        final BigDecimal totalSubsidyAmount = subsidyAmountAllocatedInStage1.subtract(BigDecimal.ONE);

        calculateRemainder(totalSubsidyAmount, inputAllocations);
    }

    @Test
    public void testCalculateRemainder_withZeroRemainder() {
        final List<RhySubsidyStage2DTO> output = calculateRemainder(subsidyAmountAllocatedInStage1, inputAllocations);

        assertEquals(tuplesFromInput(inputAllocations), tuplesFromOutput(output));
    }

    private static List<Tuple3<String, Integer, BigDecimal>> tuplesFromInput(final List<RhySubsidyStage1DTO> list) {
        return F.mapNonNullsToList(list, dto ->
                Tuple.of(dto.getRhyCode(), 0, dto.getCalculation().getTotalRoundedShare()));
    }

    private static List<Tuple3<String, Integer, BigDecimal>> tuplesFromOutput(final List<RhySubsidyStage2DTO> list) {
        return F.mapNonNullsToList(list, dto -> Tuple.of(
                dto.getRhyCode(),
                dto.getCalculation().getRemainderEurosGivenInStage2(),
                dto.getCalculation().getSubsidyAfterStage2RemainderAllocation()));
    }

    private static RhySubsidyStage1DTO newInputAllocation(final String rhyCode, final BigDecimal totalRoundedShare) {
        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        return new RhySubsidyStage1DTO(
                new RhyAndRkaDTO(rhy, rka),
                new SubsidyCalculationStage1DTO(
                        createEmptyCalculatedShares(), // not relevant in tests of stage 2
                        totalRoundedShare));
    }
}
