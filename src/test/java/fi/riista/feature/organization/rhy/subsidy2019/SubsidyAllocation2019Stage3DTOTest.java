package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019InputDTO;
import org.junit.Test;

import java.math.BigDecimal;

import static fi.riista.feature.organization.rhy.subsidy.RhySubsidyTestHelper.createEmptyCalculatedShares;
import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubsidyAllocation2019Stage3DTOTest {

    @Test
    public void testToCompensationInput() {
        final SubsidyAllocation2019Stage3DTO allocation =
                newInputAllocation("123", currency(10), currency(6), currency(8));

        final SubsidyCompensation2019InputDTO compensationInput = allocation.toCompensationInput();

        assertEquals("123", compensationInput.getRhyCode());
        assertEquals(currency(10 + 6), compensationInput.getTotalSubsidyCalculatedForCurrentYear());
        assertEquals(currency(6), compensationInput.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(currency(7), compensationInput.getSubsidyLowerLimitBasedOnLastYear());
        assertFalse(compensationInput.isAlreadyCompensated());
    }

    private static SubsidyAllocation2019Stage3DTO newInputAllocation(final String rhyCode,
                                                                     final BigDecimal calculatedSubsidyForSecondBatch,
                                                                     final BigDecimal subsidyGrantedInFirstBatch,
                                                                     final BigDecimal subsidyGrantedLastYear) {

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setOfficialCode(rhyCode);

        final SubsidyBatch2019InfoDTO subsidyBatchInfo = SubsidyBatch2019InfoDTO
                .create(calculatedSubsidyForSecondBatch, subsidyGrantedInFirstBatch, subsidyGrantedLastYear);

        return new SubsidyAllocation2019Stage3DTO(
                rhy,
                new OrganisationNameDTO(),       // not relevant in tests of this class
                createEmptyCalculatedShares(),   // not relevant in tests of this class
                calculatedSubsidyForSecondBatch,
                0,                               // not relevant in tests of this class
                subsidyBatchInfo);
    }
}
