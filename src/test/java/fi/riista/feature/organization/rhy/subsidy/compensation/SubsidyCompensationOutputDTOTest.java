package fi.riista.feature.organization.rhy.subsidy.compensation;

import org.junit.Test;

import java.math.BigDecimal;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.test.TestUtils.currency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SubsidyCompensationOutputDTOTest {

    private final SubsidyCompensationOutputDTO aboveLowerLimitAfterDecrease = createDecreased("002", 9, 5, 8, 1);
    private final SubsidyCompensationOutputDTO atLowerLimitAfterDecrease = createDecreased("003", 8, 5, 8, 2);
    private final SubsidyCompensationOutputDTO belowLowerLimitAfterDecrease = createDecreased("004", 7, 5, 8, 3);
    private final SubsidyCompensationOutputDTO negativeSecondBatchAfterDecrease = createDecreased("005", 5, 6, 4, 5);

    @Test
    public void testIncreased() {
        final SubsidyCompensationInputDTO in = SubsidyCompensationInputs.create("001", 2, 1, 3);

        final BigDecimal increment = currency(1);
        final SubsidyCompensationOutputDTO out = SubsidyCompensationOutputDTO.increased(in, increment);

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
        SubsidyCompensationOutputDTO.increased(SubsidyCompensationInputs.TOTAL_SUBSIDY_BELOW_LOWER_LIMIT, currency(-1));
    }

    @Test
    public void testDecreased() {
        final SubsidyCompensationInputDTO in = SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT;

        final BigDecimal decrement = currency(1);
        final SubsidyCompensationOutputDTO out = SubsidyCompensationOutputDTO.decreased(in, decrement);

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
        SubsidyCompensationOutputDTO.decreased(SubsidyCompensationInputs.TOTAL_SUBSIDY_ABOVE_LOWER_LIMIT, currency(-1));
    }

    @Test
    public void testKeepSubsidyUnchanged() {
        testKeepSubsidyUnchanged(true);
        testKeepSubsidyUnchanged(false);
    }

    private static void testKeepSubsidyUnchanged(final boolean alreadyCompensated) {
        final SubsidyCompensationInputDTO in = SubsidyCompensationInputs.create("001", 8, 4, 8, alreadyCompensated);

        final SubsidyCompensationOutputDTO out = SubsidyCompensationOutputDTO.keepSubsidyUnchanged(in);

        assertEquals(in.getRhyCode(), out.getRhyCode());
        assertEquals(in.getTotalSubsidyCalculatedForCurrentYear(), out.getTotalSubsidyAfterCompensation());
        assertEquals(in.getSubsidyGrantedInFirstBatchOfCurrentYear(), out.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(in.getSubsidyLowerLimitBasedOnLastYear(), out.getSubsidyLowerLimitBasedOnLastYear());
        assertNull(out.getDecrement());
        assertFalse(out.isDownscaled());

        assertTransformedInput(out, alreadyCompensated);
    }

    @Test
    public void testCountDifferenceOfTotalCalculatedSubsidyToLowerLimit() {
        assertEquals(currency(1), aboveLowerLimitAfterDecrease.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
        assertEquals(ZERO_MONETARY_AMOUNT, atLowerLimitAfterDecrease.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
        assertEquals(currency(-1), belowLowerLimitAfterDecrease.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());

        assertEquals(currency(1), negativeSecondBatchAfterDecrease.countDifferenceOfTotalCalculatedSubsidyToLowerLimit());
    }

    @Test
    public void testGetCalculatedSubsidyForSecondBatch() {
        assertEquals(currency(4), aboveLowerLimitAfterDecrease.getCalculatedSubsidyForSecondBatch());
        assertEquals(currency(3), atLowerLimitAfterDecrease.getCalculatedSubsidyForSecondBatch());
        assertEquals(currency(2), belowLowerLimitAfterDecrease.getCalculatedSubsidyForSecondBatch());

        assertEquals(currency(-1), negativeSecondBatchAfterDecrease.getCalculatedSubsidyForSecondBatch());
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

    private static void assertTransformedInput(final SubsidyCompensationOutputDTO output,
                                               final boolean shouldBeTaggedAlreadyCompensated) {

        final SubsidyCompensationInputDTO transformed = output.toInputForAnotherCompensationRound();

        assertEquals(output.getRhyCode(), transformed.getRhyCode());
        assertEquals(output.getTotalSubsidyAfterCompensation(), transformed.getTotalSubsidyCalculatedForCurrentYear());
        assertEquals(
                output.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                transformed.getSubsidyGrantedInFirstBatchOfCurrentYear());
        assertEquals(
                output.getSubsidyLowerLimitBasedOnLastYear(), transformed.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(shouldBeTaggedAlreadyCompensated, transformed.isAlreadyCompensated());
    }

    private static SubsidyCompensationOutputDTO createDecreased(final String rhyCode,
                                                                final int calculatedSubsidy,
                                                                final int subsidyGrantedInFirstBatch,
                                                                final int subsidyLowerLimitBasedOnLastYear,
                                                                final int decrement) {

        return new SubsidyCompensationOutputDTO(
                rhyCode,
                currency(calculatedSubsidy),
                currency(subsidyGrantedInFirstBatch),
                currency(subsidyLowerLimitBasedOnLastYear),
                currency(decrement));
    }
}
