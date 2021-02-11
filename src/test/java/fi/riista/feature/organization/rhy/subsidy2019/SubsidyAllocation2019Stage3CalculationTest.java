package fi.riista.feature.organization.rhy.subsidy2019;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.PreviouslyGrantedSubsidiesDTO;
import fi.riista.feature.organization.rhy.subsidy.RhyAndRkaDTO;
import fi.riista.feature.organization.rhy.subsidy.RhySubsidyStage2DTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculationStage2DTO;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.feature.organization.rhy.subsidy2019.SubsidyAllocation2019Stage3Calculation.addSubsidyBatchInfo;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocation2019Stage3CalculationTest implements ValueGeneratorMixin {

    private static final String RHY1_CODE = "001";
    private static final String RHY2_CODE = "002";
    private static final String RHY3_CODE = "003";

    private static OrganisationNameDTO rka;

    @BeforeClass
    public static void setUp() {
        rka = new OrganisationNameDTO();
        rka.setOfficialCode("000");
    }

    @Test
    public void testAddSubsidyBatchInfo() {
        final List<RhySubsidyStage2DTO> inputAllocations = asList(
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

        final List<SubsidyAllocation2019Stage3DTO> resultAllocations =
                addSubsidyBatchInfo(inputAllocations, new PreviouslyGrantedSubsidiesDTO(
                        subsidiesGrantedLastYear, subsidiesGrantedInFirstBatchOfCurrentYear));

        assertEquals(3, resultAllocations.size());

        for (int i = 0; i < 3; i++) {
            assertPropertiesNotExpectedToChange(resultAllocations.get(i), inputAllocations.get(i));
        }

        final Map<String, SubsidyBatch2019InfoDTO> expectedBatchInfos = ImmutableMap.of(
                RHY1_CODE, new SubsidyBatch2019InfoDTO(currency(10), currency(8), currency(15), currency(12)),
                RHY2_CODE, new SubsidyBatch2019InfoDTO(currency(7), currency(13), currency(13), currency(11)),
                RHY3_CODE, new SubsidyBatch2019InfoDTO(currency(5), currency(2), currency(11), currency(9)));

        final Map<String, SubsidyBatch2019InfoDTO> actualBatchInfos = resultAllocations
                .stream()
                .collect(toMap(
                        SubsidyAllocation2019Stage3DTO::getRhyCode,
                        SubsidyAllocation2019Stage3DTO::getSubsidyBatchInfo));

        assertEquals(expectedBatchInfos, actualBatchInfos);
    }

    private static void assertPropertiesNotExpectedToChange(final SubsidyAllocation2019Stage3DTO output,
                                                            final RhySubsidyStage2DTO input) {

        assertEquals(input.getOrganisationInfo().getRhy(), output.getRhy());
        assertEquals(input.getOrganisationInfo().getRka(), output.getRka());

        final SubsidyCalculationStage2DTO inputCalc = input.getCalculation();

        assertEquals(inputCalc.getCalculatedShares(), output.getCalculatedShares());
        assertEquals(inputCalc.getSubsidyAfterStage2RemainderAllocation(), output.getTotalRoundedShare());
        assertEquals(inputCalc.getRemainderEurosGivenInStage2(), output.getRemainderEurosGivenInStage2());
    }

    private RhySubsidyStage2DTO newInputAllocation(final String rhyCode, final BigDecimal subsidyAfterStage2) {
        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        return new RhySubsidyStage2DTO(
                new RhyAndRkaDTO(rhy, rka),
                new SubsidyCalculationStage2DTO(
                        createEmptyCalculatedShares(), // not relevant in tests of stage 3
                        subsidyAfterStage2,
                        nextIntBetween(0, 2)));        // not relevant in tests of stage 3
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }
}
