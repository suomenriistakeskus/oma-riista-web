package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputDTO;
import org.junit.Test;

import java.math.BigDecimal;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubsidyAllocationStage3DTOTest {

    @Test
    public void testToCompensationInput() {
        final SubsidyAllocationStage3DTO allocation = newInputAllocation("123", currency(10), currency(6), currency(8));

        final SubsidyCompensationInputDTO compensationInput = allocation.toCompensationInput();

        assertEquals("123", compensationInput.getRhyCode());
        assertEquals(currency(10 + 6), compensationInput.getTotalSubsidyCalculatedForCurrentYear());
        assertEquals(currency(6), compensationInput.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(currency(7), compensationInput.getSubsidyLowerLimitBasedOnLastYear());
        assertFalse(compensationInput.isAlreadyCompensated());
    }

    private static SubsidyAllocationStage3DTO newInputAllocation(final String rhyCode,
                                                                 final BigDecimal calculatedSubsidyForSecondBatch,
                                                                 final BigDecimal subsidyGrantedInFirstBatch,
                                                                 final BigDecimal subsidyGrantedLastYear) {

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        final SubsidyBatchInfoDTO subsidyBatchInfo = SubsidyBatchInfoDTO
                .create(calculatedSubsidyForSecondBatch, subsidyGrantedInFirstBatch, subsidyGrantedLastYear);

        return new SubsidyAllocationStage3DTO(
                rhy,
                new OrganisationNameDTO(),       // not relevant in tests of this class
                createEmptyCalculatedShares(),   // not relevant in tests of this class
                calculatedSubsidyForSecondBatch,
                0,                               // not relevant in tests of this class
                subsidyBatchInfo);
    }
}
