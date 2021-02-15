package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import org.junit.Test;

import java.math.BigDecimal;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SubsidyCompensation2019OutputDTOTest {

    private final SubsidyCompensation2019OutputDTO aboveLowerLimitAfterDecrease = createDecreased("002", 9, 5, 8, 1);
    private final SubsidyCompensation2019OutputDTO atLowerLimitAfterDecrease = createDecreased("003", 8, 5, 8, 2);
    private final SubsidyCompensation2019OutputDTO belowLowerLimitAfterDecrease = createDecreased("004", 7, 5, 8, 3);
    private final SubsidyCompensation2019OutputDTO negativeSecondBatchAfterDecrease = createDecreased("005", 5, 6, 4, 5);

    @Test
    public void testIncreased() {
        final SubsidyCompensation2019InputDTO in = SubsidyCompensation2019Inputs.create("001", 2, 1, 3);

        final BigDecimal increment = currency(1);
        final SubsidyCompensation2019OutputDTO out = SubsidyCompensation2019OutputDTO.increased(in, increment);

        assertEquals(in.getRhyCode(), out.getRhyCode());

        final BigDecimal incresedSubsidy = in.getTotalSubsidyCalculatedForCurrentYear().add(increment);

        assertEquals(incresedSubsidy, out.getTotalSubsidyAfterCompensation());
        assertEquals(in.getSubsidyGrantedInFirstBatchOfCurrentYear(), out.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(in.getSubsidyLowerLimitBasedOnLastYear(), out.getSubsidyLowerLimitBasedOnLastYear());
        assertNull(out.getDecrement());
        assertFalse(out.isDownscaled());

        assertTransformedInput(out, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncreased_withNegativeIncrement() {
        SubsidyCompensation2019OutputDTO.increased(SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT, currency(-1));
    }

    @Test
    public void testDecreased() {
        final SubsidyCompensation2019InputDTO in = SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT;

        final BigDecimal decrement = currency(1);
        final SubsidyCompensation2019OutputDTO out = SubsidyCompensation2019OutputDTO.decreased(in, decrement);

        assertEquals(in.getRhyCode(), out.getRhyCode());

        final BigDecimal decresedSubsidy = in.getTotalSubsidyCalculatedForCurrentYear().subtract(decrement);

        assertEquals(decresedSubsidy, out.getTotalSubsidyAfterCompensation());
        assertEquals(in.getSubsidyGrantedInFirstBatchOfCurrentYear(), out.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(in.getSubsidyLowerLimitBasedOnLastYear(), out.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(decrement, out.getDecrement());
        assertTrue(out.isDownscaled());

        assertTransformedInput(out, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecreased_withNegativeDecrement() {
        SubsidyCompensation2019OutputDTO.decreased(SubsidyCompensation2019Inputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT, currency(-1));
    }

    @Test
    public void testKeepSubsidyUnchanged() {
        testKeepSubsidyUnchanged(true);
        testKeepSubsidyUnchanged(false);
    }

    private static void testKeepSubsidyUnchanged(final boolean alreadyCompensated) {
        final SubsidyCompensation2019InputDTO in = SubsidyCompensation2019Inputs.create("001", 8, 4, 8, alreadyCompensated);

        final SubsidyCompensation2019OutputDTO out = SubsidyCompensation2019OutputDTO.keepSubsidyUnchanged(in);

        assertEquals(in.getRhyCode(), out.getRhyCode());
        assertEquals(in.getTotalSubsidyCalculatedForCurrentYear(), out.getTotalSubsidyAfterCompensation());
        assertEquals(in.getSubsidyGrantedInFirstBatchOfCurrentYear(), out.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(in.getSubsidyLowerLimitBasedOnLastYear(), out.getSubsidyLowerLimitBasedOnLastYear());
        assertNull(out.getDecrement());
        assertFalse(out.isDownscaled());

        assertTransformedInput(out, alreadyCompensated);
    }

    @Test
    public void testCountDifferenceOfCalculatedSubsidyToLowerLimit() {
        assertEquals(currency(1), aboveLowerLimitAfterDecrease.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(ZERO_MONETARY_AMOUNT, atLowerLimitAfterDecrease.countDifferenceOfCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-1), belowLowerLimitAfterDecrease.countDifferenceOfCalculatedSubsidyToLowerLimit());

        assertEquals(currency(1), negativeSecondBatchAfterDecrease.countDifferenceOfCalculatedSubsidyToLowerLimit());
    }

    @Test
    public void testGetCalculatedSubsidy() {
        assertEquals(currency(4), aboveLowerLimitAfterDecrease.getCalculatedSubsidy());
        assertEquals(currency(3), atLowerLimitAfterDecrease.getCalculatedSubsidy());
        assertEquals(currency(2), belowLowerLimitAfterDecrease.getCalculatedSubsidy());

        assertEquals(currency(-1), negativeSecondBatchAfterDecrease.getCalculatedSubsidy());
    }

    @Test
    public void testIsExactlyAtLowerLimit() {
        assertFalse(aboveLowerLimitAfterDecrease.isExactlyAtLowerLimit());
        assertTrue(atLowerLimitAfterDecrease.isExactlyAtLowerLimit());
        assertFalse(belowLowerLimitAfterDecrease.isExactlyAtLowerLimit());

        assertFalse(negativeSecondBatchAfterDecrease.isExactlyAtLowerLimit());
    }

    @Test
    public void testIsCompensationNeeded() {
        assertFalse(aboveLowerLimitAfterDecrease.isCompensationNeeded());
        assertFalse(atLowerLimitAfterDecrease.isCompensationNeeded());
        assertTrue(belowLowerLimitAfterDecrease.isCompensationNeeded());

        assertTrue(negativeSecondBatchAfterDecrease.isCompensationNeeded());
    }

    @Test
    public void testCountAmountOfCompensationNeed() {
        assertEquals(ZERO_MONETARY_AMOUNT, aboveLowerLimitAfterDecrease.countAmountOfCompensationNeed());
        assertEquals(ZERO_MONETARY_AMOUNT, atLowerLimitAfterDecrease.countAmountOfCompensationNeed());
        assertEquals(currency(1), belowLowerLimitAfterDecrease.countAmountOfCompensationNeed());

        assertEquals(currency(1), negativeSecondBatchAfterDecrease.countAmountOfCompensationNeed());
    }

    private static void assertTransformedInput(final SubsidyCompensation2019OutputDTO output,
                                               final boolean shouldBeTaggedAlreadyCompensated) {

        final SubsidyCompensation2019InputDTO transformed = output.toInputForAnotherCompensationRound();

        assertEquals(output.getRhyCode(), transformed.getRhyCode());
        assertEquals(output.getTotalSubsidyAfterCompensation(), transformed.getTotalSubsidyCalculatedForCurrentYear());
        assertEquals(
                output.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                transformed.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(
                output.getSubsidyLowerLimitBasedOnLastYear(), transformed.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(shouldBeTaggedAlreadyCompensated, transformed.isAlreadyCompensated());
    }

    private static SubsidyCompensation2019OutputDTO createDecreased(final String rhyCode,
                                                                    final int calculatedSubsidy,
                                                                    final int subsidyGrantedInFirstBatch,
                                                                    final int subsidyLowerLimitBasedOnLastYear,
                                                                    final int decrement) {

        return new SubsidyCompensation2019OutputDTO(
                rhyCode,
                currency(calculatedSubsidy),
                currency(subsidyGrantedInFirstBatch),
                currency(subsidyLowerLimitBasedOnLastYear),
                currency(decrement));
    }
}
