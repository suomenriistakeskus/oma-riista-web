package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage3Calculation.addSubsidyBatchInfo;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationStage3CalculationTest implements ValueGeneratorMixin {

    private static final String RHY1_CODE = "001";
    private static final String RHY2_CODE = "002";
    private static final String RHY3_CODE = "003";

    @Test
    public void testAddSubsidyBatchInfo() {
        final List<BasicSubsidyAllocationDTO> inputAllocations = asList(
                newInputAllocation(RHY1_CODE, currency(10)),
                newInputAllocation(RHY2_CODE, currency(7)),
                newInputAllocation(RHY3_CODE, currency(5)));

        final Map<String, BigDecimal> subsidiesGrantedInFirstBatchOfCurrentYear = ImmutableMap.of(
                RHY1_CODE, currency(8),
                RHY2_CODE, currency(13),
                RHY3_CODE, currency(2));

        final Map<String, BigDecimal> subsidiesGrantedLastYear = ImmutableMap.of(
                RHY1_CODE, currency(15),
                RHY2_CODE, currency(13),
                RHY3_CODE, currency(11));

        final List<SubsidyAllocationStage3DTO> resultAllocations =
                addSubsidyBatchInfo(inputAllocations, new PreviouslyGrantedSubsidiesDTO(
                        subsidiesGrantedLastYear, subsidiesGrantedInFirstBatchOfCurrentYear));

        assertEquals(3, resultAllocations.size());

        for (int i = 0; i < 3; i++) {
            assertPropertiesNotExpectedToChange(resultAllocations.get(i), inputAllocations.get(i));
        }

        final Map<String, SubsidyBatchInfoDTO> expectedBatchInfos = ImmutableMap.of(
                RHY1_CODE, new SubsidyBatchInfoDTO(currency(10), currency(8), currency(15), currency(12)),
                RHY2_CODE, new SubsidyBatchInfoDTO(currency(7), currency(13), currency(13), currency(11)),
                RHY3_CODE, new SubsidyBatchInfoDTO(currency(5), currency(2), currency(11), currency(9)));

        final Map<String, SubsidyBatchInfoDTO> actualBatchInfos = resultAllocations
                .stream()
                .collect(toMap(
                        SubsidyAllocationStage3DTO::getRhyCode, SubsidyAllocationStage3DTO::getSubsidyBatchInfo));

        assertEquals(expectedBatchInfos, actualBatchInfos);
    }

    private static void assertPropertiesNotExpectedToChange(final SubsidyAllocationStage3DTO output,
                                                            final BasicSubsidyAllocationDTO input) {

        // Comparing by object identities here since these properties are expected to be copied
        // from input to output.
        assertEquals(input.getRhy(), output.getRhy());
        assertEquals(input.getRka(), output.getRka());
        assertEquals(input.getCalculatedShares(), output.getCalculatedShares());
        assertEquals(input.getTotalRoundedShare(), output.getTotalRoundedShare());
        assertEquals(input.getGivenRemainderEuros(), output.getRemainderEurosGivenInStage2());
    }

    private BasicSubsidyAllocationDTO newInputAllocation(final String rhyCode,
                                                         final BigDecimal totalRoundedShareAfterStage2) {

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        return new BasicSubsidyAllocationDTO(
                rhy,
                new OrganisationNameDTO(),     // not relevant in tests of this class
                createEmptyCalculatedShares(), // not relevant in tests of this class
                totalRoundedShareAfterStage2,
                nextIntBetween(0, 2));         // not relevant in tests of this class
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }
}
