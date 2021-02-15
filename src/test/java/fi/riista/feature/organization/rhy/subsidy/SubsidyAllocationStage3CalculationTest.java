package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage3Calculation.addSubsidyComparisonToLastYear;
import static fi.riista.test.TestUtils.currency;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class SubsidyAllocationStage3CalculationTest implements ValueGeneratorMixin {

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
    public void testAddSubsidyComparisonToLastYear() {
        final List<RhySubsidyStage2DTO> inputAllocations = asList(
                newInputAllocation(RHY1_CODE, currency(10)),
                newInputAllocation(RHY2_CODE, currency(7)),
                newInputAllocation(RHY3_CODE, currency(5)));

        final Map<String, BigDecimal> subsidiesGrantedLastYear = ImmutableMap.of(
                RHY1_CODE, currency(15),
                RHY2_CODE, currency(13),
                RHY3_CODE, currency(11));

        final List<RhySubsidyStage3DTO> resultAllocations = addSubsidyComparisonToLastYear(
                inputAllocations, new PreviouslyGrantedSubsidiesDTO(subsidiesGrantedLastYear, emptyMap()));

        assertEquals(3, resultAllocations.size());

        for (int i = 0; i < 3; i++) {
            assertCopiedProperties(resultAllocations.get(i), inputAllocations.get(i));
        }

        final Map<String, SubsidyComparisonToLastYearDTO> expectedComparisons = ImmutableMap.of(
                RHY1_CODE, new SubsidyComparisonToLastYearDTO(currency(10), currency(15), currency(12)),
                RHY2_CODE, new SubsidyComparisonToLastYearDTO(currency(7), currency(13), currency(11)),
                RHY3_CODE, new SubsidyComparisonToLastYearDTO(currency(5), currency(11), currency(9)));

        final Map<String, SubsidyComparisonToLastYearDTO> actualComparisons = resultAllocations
                .stream()
                .collect(toMap(
                        RhySubsidyStage3DTO::getRhyCode,
                        dto -> dto.getCalculation().getSubsidyComparisonToLastYear()));

        assertEquals(expectedComparisons, actualComparisons);
    }

    private static void assertCopiedProperties(final RhySubsidyStage3DTO output, final RhySubsidyStage2DTO input) {
        assertEquals(input.getOrganisationInfo(), output.getOrganisationInfo());

        final SubsidyCalculationStage2DTO inputCalc = input.getCalculation();
        final SubsidyCalculationStage3DTO outputCalc = output.getCalculation();

        assertEquals(inputCalc.getCalculatedShares(), outputCalc.getCalculatedShares());
        assertEquals(inputCalc.getSubsidyAfterStage2RemainderAllocation(), outputCalc.getSubsidyAfterStage2RemainderAllocation());
        assertEquals(inputCalc.getRemainderEurosGivenInStage2(), outputCalc.getRemainderEurosGivenInStage2());
    }

    private RhySubsidyStage2DTO newInputAllocation(final String rhyCode, final BigDecimal subsidyAfterStage2) {
        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        return new RhySubsidyStage2DTO(
                new RhyAndRkaDTO(rhy, rka),
                new SubsidyCalculationStage2DTO(
                        createEmptyCalculatedShares(), // not relevant in tests of stage 3
                        subsidyAfterStage2,
                        nextIntBetween(0, 2)));
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }
}
