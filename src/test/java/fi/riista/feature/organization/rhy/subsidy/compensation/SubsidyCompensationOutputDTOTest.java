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

    private final SubsidyCompensationOutputDTO aboveLowerLimitAfterDecrease = createDecreased("002", 9, 8, 1);
    private final SubsidyCompensationOutputDTO atLowerLimitAfterDecrease = createDecreased("003", 8, 8, 2);
    private final SubsidyCompensationOutputDTO belowLowerLimitAfterDecrease = createDecreased("004", 7, 8, 3);

    @Test
    public void testIncreased() {
        final SubsidyCompensationInputDTO in = SubsidyCompensationInputs.create("001", 2, 3);

        final BigDecimal increment = currency(1);
        final SubsidyCompensationOutputDTO out = SubsidyCompensationOutputDTO.increased(in, increment);

        final BigDecimal incresedSubsidy = in.getCalculatedSubsidy().add(increment);

        assertEquals(in.getRhyCode(), out.getRhyCode());
        assertEquals(incresedSubsidy, out.getSubsidyAfterCompensation());
        assertEquals(incresedSubsidy, out.getCalculatedSubsidy());
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

        final BigDecimal decresedSubsidy = in.getCalculatedSubsidy().subtract(decrement);

        assertEquals(in.getRhyCode(), out.getRhyCode());
        assertEquals(decresedSubsidy, out.getSubsidyAfterCompensation());
        assertEquals(decresedSubsidy, out.getCalculatedSubsidy());
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
        final SubsidyCompensationInputDTO in = SubsidyCompensationInputs.create("001", 8, 8, alreadyCompensated);
        final SubsidyCompensationOutputDTO out = SubsidyCompensationOutputDTO.keepSubsidyUnchanged(in);

        assertEquals(in.getRhyCode(), out.getRhyCode());
        assertEquals(in.getCalculatedSubsidy(), out.getSubsidyAfterCompensation());
        assertEquals(in.getCalculatedSubsidy(), out.getCalculatedSubsidy());
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
    }

    @Test
    public void testIsExactlyAtLowerLimit() {
        assertFalse(aboveLowerLimitAfterDecrease.isExactlyAtLowerLimit());
        assertTrue(atLowerLimitAfterDecrease.isExactlyAtLowerLimit());
        assertFalse(belowLowerLimitAfterDecrease.isExactlyAtLowerLimit());
    }

    @Test
    public void testIsCompensationNeeded() {
        assertFalse(aboveLowerLimitAfterDecrease.isCompensationNeeded());
        assertFalse(atLowerLimitAfterDecrease.isCompensationNeeded());
        assertTrue(belowLowerLimitAfterDecrease.isCompensationNeeded());
    }

    @Test
    public void testCountAmountOfCompensationNeed() {
        assertEquals(ZERO_MONETARY_AMOUNT, aboveLowerLimitAfterDecrease.countAmountOfCompensationNeed());
        assertEquals(ZERO_MONETARY_AMOUNT, atLowerLimitAfterDecrease.countAmountOfCompensationNeed());
        assertEquals(currency(1), belowLowerLimitAfterDecrease.countAmountOfCompensationNeed());
    }

    private static void assertTransformedInput(final SubsidyCompensationOutputDTO dto,
                                               final boolean shouldBeTaggedAlreadyCompensated) {

        final SubsidyCompensationInputDTO transformed = dto.toInputForAnotherCompensationRound();

        assertEquals(dto.getRhyCode(), transformed.getRhyCode());
        assertEquals(dto.getSubsidyAfterCompensation(), transformed.getCalculatedSubsidy());
        assertEquals(dto.getSubsidyLowerLimitBasedOnLastYear(), transformed.getSubsidyLowerLimitBasedOnLastYear());
        assertEquals(shouldBeTaggedAlreadyCompensated, transformed.isAlreadyCompensated());
    }

    private static SubsidyCompensationOutputDTO createDecreased(final String rhyCode,
                                                                final int calculatedSubsidy,
                                                                final int subsidyLowerLimitBasedOnLastYear,
                                                                final int decrement) {
        return new SubsidyCompensationOutputDTO(
                rhyCode, currency(calculatedSubsidy), currency(subsidyLowerLimitBasedOnLastYear), currency(decrement));
    }
}
