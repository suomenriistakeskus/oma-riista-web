package fi.riista.feature.organization.rhy.subsidy.compensation;

import org.junit.Test;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputs.NEGATIVE_SECOND_BATCH;
import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputs.NEGATIVE_SECOND_BATCH_AND_TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;
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

    private final SubsidyCompensationInputDTO hasNegativeSecondBatch = NEGATIVE_SECOND_BATCH;
    private final SubsidyCompensationInputDTO hasNegativeSecondBatchAndDiff =
            NEGATIVE_SECOND_BATCH_AND_TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;

    @Test
    public void testConstructor() {
        testConstructor(true);
        testConstructor(false);
    }

    private static void testConstructor(final boolean alreadyCompensated) {
        final String rhyCode = "001";
        final int calculatedSubsidy = 1200;
        final int subsidyGrantedInBatch1 = 500;
        final int lowerLimitBasedOnLastYear = 800;

        final SubsidyCompensationInputDTO dto = SubsidyCompensationInputs.create(
                rhyCode, calculatedSubsidy, subsidyGrantedInBatch1, lowerLimitBasedOnLastYear, alreadyCompensated);

        assertEquals(rhyCode, dto.getRhyCode());
        assertEquals(currency(calculatedSubsidy), dto.getTotalSubsidyCalculatedForCurrentYear());
        assertEquals(currency(subsidyGrantedInBatch1), dto.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(currency(lowerLimitBasedOnLastYear), dto.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(alreadyCompensated, dto.isAlreadyCompensated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_alreadyCompensatedShouldNotHaveCompensationNeed() {
        new SubsidyCompensationInputDTO("001", currency(10), currency(5), currency(11), true);
    }

    @Test
    public void testCountDifferenceOfTotalCalculatedSubsidyToLowerLimit() {
        assertEquals(currency(4), hasPositiveDiff.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
        assertEquals(currency(0), hasZeroDiff.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-2), hasNegativeDiff.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());

        assertEquals(currency(2), hasNegativeSecondBatch.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-4), hasNegativeSecondBatchAndDiff.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
    }

    @Test
    public void testGetCalculatedSubsidyForSecondBatch() {
        assertEquals(currency(6), hasPositiveDiff.getCalculatedSubsidyForSecondBatch());
        assertEquals(currency(5), hasZeroDiff.getCalculatedSubsidyForSecondBatch());
        assertEquals(currency(0), hasNegativeDiff.getCalculatedSubsidyForSecondBatch());

        assertEquals(currency(-2), hasNegativeSecondBatch.getCalculatedSubsidyForSecondBatch());
        assertEquals(currency(-6), hasNegativeSecondBatchAndDiff.getCalculatedSubsidyForSecondBatch());
    }

    @Test
    public void testIsExactlyAtLowerLimit() {
        assertFalse(hasPositiveDiff.isExactlyAtLowerLimit());
        assertTrue(hasZeroDiff.isExactlyAtLowerLimit());
        assertFalse(hasNegativeDiff.isExactlyAtLowerLimit());

        assertFalse(hasNegativeSecondBatch.isExactlyAtLowerLimit());
        assertFalse(hasNegativeSecondBatchAndDiff.isExactlyAtLowerLimit());
    }

    @Test
    public void testCountAmountOfCompensationNeed() {
        assertEquals(ZERO_MONETARY_AMOUNT, hasPositiveDiff.countAmountOfCompensationNeed());
        assertEquals(ZERO_MONETARY_AMOUNT, hasZeroDiff.countAmountOfCompensationNeed());
        assertEquals(currency(2), hasNegativeDiff.countAmountOfCompensationNeed());

        assertEquals(currency(2), hasNegativeSecondBatch.countAmountOfCompensationNeed());
        assertEquals(currency(6), hasNegativeSecondBatchAndDiff.countAmountOfCompensationNeed());
    }

    @Test
    public void testIsCompensationNeeded() {
        assertFalse(hasPositiveDiff.isCompensationNeeded());
        assertFalse(hasZeroDiff.isCompensationNeeded());
        assertTrue(hasNegativeDiff.isCompensationNeeded());

        assertTrue(hasNegativeSecondBatch.isCompensationNeeded());
        assertTrue(hasNegativeSecondBatchAndDiff.isCompensationNeeded());
    }
}
