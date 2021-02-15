package fi.riista.feature.organization.rhy.subsidy.compensation;

import org.junit.Test;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT;
import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;
import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT;
import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubsidyCompensationInputDTOTest {

    private final SubsidyCompensationInputDTO hasPositiveDiff = TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT;
    private final SubsidyCompensationInputDTO hasZeroDiff = TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT;
    private final SubsidyCompensationInputDTO hasNegativeDiff = TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;

    @Test
    public void testConstructor() {
        testConstructor(true);
        testConstructor(false);
    }

    private static void testConstructor(final boolean alreadyCompensated) {
        final String rhyCode = "001";
        final int calculatedSubsidy = 1200;
        final int lowerLimitBasedOnLastYear = 800;

        final SubsidyCompensationInputDTO dto = SubsidyCompensationInputs.create(
                rhyCode, calculatedSubsidy, lowerLimitBasedOnLastYear, alreadyCompensated);

        assertEquals(rhyCode, dto.getRhyCode());
        assertEquals(currency(calculatedSubsidy), dto.getCalculatedSubsidy());
        assertEquals(currency(lowerLimitBasedOnLastYear), dto.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(alreadyCompensated, dto.isAlreadyCompensated());
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_alreadyCompensatedShouldNotHaveCompensationNeed() {
        new SubsidyCompensationInputDTO("001", currency(10), currency(11), true);
    }

    @Test
    public void testCountDifferenceOfCalculatedSubsidyToLowerLimit() {
        assertEquals(currency(4), hasPositiveDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(currency(0), hasZeroDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-2), hasNegativeDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());
    }

    @Test
    public void testIsExactlyAtLowerLimit() {
        assertFalse(hasPositiveDiff.isExactlyAtLowerLimit());
        assertTrue(hasZeroDiff.isExactlyAtLowerLimit());
        assertFalse(hasNegativeDiff.isExactlyAtLowerLimit());
    }

    @Test
    public void testCountAmountOfCompensationNeed() {
        assertEquals(ZERO_MONETARY_AMOUNT, hasPositiveDiff.countAmountOfCompensationNeed());
        assertEquals(ZERO_MONETARY_AMOUNT, hasZeroDiff.countAmountOfCompensationNeed());
        assertEquals(currency(2), hasNegativeDiff.countAmountOfCompensationNeed());
    }

    @Test
    public void testIsCompensationNeeded() {
        assertFalse(hasPositiveDiff.isCompensationNeeded());
        assertFalse(hasZeroDiff.isCompensationNeeded());
        assertTrue(hasNegativeDiff.isCompensationNeeded());
    }
}
