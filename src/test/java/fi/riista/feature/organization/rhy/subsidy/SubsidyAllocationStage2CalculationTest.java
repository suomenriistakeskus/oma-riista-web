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
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage2Calculation.calculateAndAllocateRemainder;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.NumberUtils.currencySum;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationStage2CalculationTest {

    private static final String RHY1_CODE = "001";
    private static final String RHY2_CODE = "002";
    private static final String RHY3_CODE = "003";
    private static final String RHY4_CODE = "004";

    private List<BasicSubsidyAllocationDTO> inputAllocations;

    private BigDecimal subsidyAmountAlreadyAllocatedInPreviousStage;

    @Before
    public void setup() {
        inputAllocations = asList(
                newInputAllocation(RHY1_CODE, currency(100)),
                newInputAllocation(RHY2_CODE, currency(101)),
                newInputAllocation(RHY3_CODE, currency(102)),
                newInputAllocation(RHY4_CODE, currency(103)));

        subsidyAmountAlreadyAllocatedInPreviousStage =
                currencySum(inputAllocations, BasicSubsidyAllocationDTO::getTotalRoundedShare);
    }

    @Test
    public void testCalculateAndAllocateRemainder_whenThereAreLessRemainderEurosThanNumberOfRhys() {
        final int numRhys = inputAllocations.size();
        final BigDecimal totalSubsidyAmount = subsidyAmountAlreadyAllocatedInPreviousStage.add(currency(numRhys - 1));

        final List<BasicSubsidyAllocationDTO> output =
                calculateAndAllocateRemainder(totalSubsidyAmount, inputAllocations);

        final List<Tuple3<String, Integer, BigDecimal>> expectedOutput = ImmutableList.of(
                Tuple.of(RHY1_CODE, 1, currency(101)),
                Tuple.of(RHY2_CODE, 1, currency(102)),
                Tuple.of(RHY3_CODE, 1, currency(103)),
                Tuple.of(RHY4_CODE, 0, currency(103)));

        assertEquals(expectedOutput, transformToTuples(output));
    }

    @Test
    public void testCalculateAndAllocateRemainder_whenThereAreMoreRemainderEurosThanNumberOfRhys() {
        final int numRhys = inputAllocations.size();
        final BigDecimal totalSubsidyAmount = subsidyAmountAlreadyAllocatedInPreviousStage.add(currency(numRhys + 1));

        final List<BasicSubsidyAllocationDTO> output =
                calculateAndAllocateRemainder(totalSubsidyAmount, inputAllocations);

        final List<Tuple3<String, Integer, BigDecimal>> expectedOutput = ImmutableList.of(
                Tuple.of(RHY1_CODE, 2, currency(102)),
                Tuple.of(RHY2_CODE, 1, currency(102)),
                Tuple.of(RHY3_CODE, 1, currency(103)),
                Tuple.of(RHY4_CODE, 1, currency(104)));

        assertEquals(expectedOutput, transformToTuples(output));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateAndAllocateRemainder_withNegativeRemainder() {
        final BigDecimal totalSubsidyAmount = subsidyAmountAlreadyAllocatedInPreviousStage.subtract(BigDecimal.ONE);

        calculateAndAllocateRemainder(totalSubsidyAmount, inputAllocations);
    }

    @Test
    public void testCalculateAndAllocateRemainder_withZeroRemainder() {
        final List<BasicSubsidyAllocationDTO> output =
                calculateAndAllocateRemainder(subsidyAmountAlreadyAllocatedInPreviousStage, inputAllocations);

        assertEquals(transformToTuples(inputAllocations), transformToTuples(output));
    }

    private static List<Tuple3<String, Integer, BigDecimal>> transformToTuples(final List<BasicSubsidyAllocationDTO> list) {
        return F.mapNonNullsToList(list, dto -> {
            return Tuple.of(dto.getRhyCode(), dto.getGivenRemainderEuros(), dto.getTotalRoundedShare());
        });
    }

    private static BasicSubsidyAllocationDTO newInputAllocation(final String rhyCode,
                                                                final BigDecimal totalRoundedShare) {

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        return new BasicSubsidyAllocationDTO(
                rhy,
                new OrganisationNameDTO(),     // not relevant content in tests of stage 2
                createEmptyCalculatedShares(), // not relevant content in tests of stage 2
                totalRoundedShare,
                0);                            // always zero after stage 1
    }
}
