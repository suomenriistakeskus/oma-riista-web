package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import org.junit.Test;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019Inputs.NEGATIVE_SECOND_BATCH;
import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019Inputs.NEGATIVE_SECOND_BATCH_AND_TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;
import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT;
import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;
import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT;
import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubsidyCompensation2019InputDTOTest {

    private final SubsidyCompensation2019InputDTO hasPositiveDiff = TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT;
    private final SubsidyCompensation2019InputDTO hasZeroDiff = TOTAL_SUBSIDY_EQUALS_TO_LOWER_LIMIT;
    private final SubsidyCompensation2019InputDTO hasNegativeDiff = TOTAL_SUBSIDY_BELOW_LOWER_LIMIT;

    private final SubsidyCompensation2019InputDTO hasNegativeSecondBatch = NEGATIVE_SECOND_BATCH;
    private final SubsidyCompensation2019InputDTO hasNegativeSecondBatchAndDiff =
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

        final SubsidyCompensation2019InputDTO dto = SubsidyCompensation2019Inputs.create(
                rhyCode, calculatedSubsidy, subsidyGrantedInBatch1, lowerLimitBasedOnLastYear, alreadyCompensated);

        assertEquals(rhyCode, dto.getRhyCode());
        assertEquals(currency(calculatedSubsidy), dto.getTotalSubsidyCalculatedForCurrentYear());
        assertEquals(currency(subsidyGrantedInBatch1), dto.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(currency(lowerLimitBasedOnLastYear), dto.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(alreadyCompensated, dto.isAlreadyCompensated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_alreadyCompensatedShouldNotHaveCompensationNeed() {
        new SubsidyCompensation2019InputDTO("001", currency(10), currency(5), currency(11), true);
    }

    @Test
    public void testCountDifferenceOfCalculatedSubsidyToLowerLimit() {
        assertEquals(currency(4), hasPositiveDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(currency(0), hasZeroDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-2), hasNegativeDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());

        assertEquals(currency(2), hasNegativeSecondBatch.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-4), hasNegativeSecondBatchAndDiff.countDifferenceOfCalculatedSubsidyToLowerLimit());
    }

    @Test
    public void testGetCalculatedSubsidy() {
        assertEquals(currency(6), hasPositiveDiff.getCalculatedSubsidy());
        assertEquals(currency(5), hasZeroDiff.getCalculatedSubsidy());
        assertEquals(currency(0), hasNegativeDiff.getCalculatedSubsidy());

        assertEquals(currency(-2), hasNegativeSecondBatch.getCalculatedSubsidy());
        assertEquals(currency(-6), hasNegativeSecondBatchAndDiff.getCalculatedSubsidy());
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
