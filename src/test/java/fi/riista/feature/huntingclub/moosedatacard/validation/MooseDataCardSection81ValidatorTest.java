package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.test.Asserts;
import org.junit.Test;

import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newSection81;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.effectiveHuntingAreaLargerThanTotalHuntingArea;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingAreaNotGivenAtAll;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.moosesRemainingInEffectiveHuntingAreaGivenButAreaMissing;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.moosesRemainingInEffectiveHuntingAreaGreaterThanMoosesRemainingInTotalHuntingArea;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.moosesRemainingInTotalHuntingAreaGivenButAreaMissing;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.moosesRemainingNotGivenAtAll;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.totalHuntingAreaMissingAndEffectiveHuntingAreaGivenAsPercentageShare;
import static fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection81Validator.validate;
import static fi.riista.test.Asserts.assertNumericFieldValidationError;
import static fi.riista.test.Asserts.assertValidationErrors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MooseDataCardSection81ValidatorTest {

    @Test
    public void testValidate_withValidData() {
        final MooseDataCardSection_8_1 section = newSection81();

        Asserts.assertValid(validate(section), s -> assertEquals(section.toString(), s.toString()));
    }

    @Test
    public void testValidate_withEmptyData() {
        assertTrue(validate(new MooseDataCardSection_8_1()).isInvalid());
    }

    @Test
    public void testValidate_withOutOfRangeNumericValues() {
        final MooseDataCardSection_8_1 section = new MooseDataCardSection_8_1()
                .withTotalHuntingArea(-1.0)
                .withEffectiveHuntingArea(-1.0)
                .withEffectiveHuntingAreaPercentage(-1.0)
                .withMoosesRemainingInTotalHuntingArea(-1)
                .withMoosesRemainingInEffectiveHuntingArea(-1);

        Asserts.assertNumericFieldValidationErrors(section, MooseDataCardSection81Validator::validate, Stream.of(
                MooseDataCardSummaryField.TOTAL_HUNTING_AREA,
                MooseDataCardSummaryField.EFFECTIVE_HUNTING_AREA,
                MooseDataCardSummaryField.EFFECTIVE_HUNTING_AREA_PERCENTAGE,
                MooseDataCardSummaryField.MOOSES_REMAINING_IN_TOTAL_HUNTING_AREA,
                MooseDataCardSummaryField.MOOSES_REMAINING_IN_EFFECTIVE_HUNTING_AREA));
    }

    @Test
    public void testValidate_whenEffectiveHuntingAreaPercentageTooLow() {
        final MooseDataCardSection_8_1 section = newSection81().withEffectiveHuntingAreaPercentage(-0.001);

        testValidate_withFieldHavingIllegalValue(section, MooseDataCardSummaryField.EFFECTIVE_HUNTING_AREA_PERCENTAGE);
    }

    @Test
    public void testValidate_whenEffectiveHuntingAreaPercentageTooHigh() {
        final MooseDataCardSection_8_1 section = newSection81().withEffectiveHuntingAreaPercentage(100.001);

        testValidate_withFieldHavingIllegalValue(section, MooseDataCardSummaryField.EFFECTIVE_HUNTING_AREA_PERCENTAGE);
    }

    @Test
    public void testValidate_whenEffectiveHuntingAreaLargerThanTotalHuntingArea() {
        final MooseDataCardSection_8_1 section = newSection81();
        final double totalHuntingArea = section.getTotalHuntingArea();
        final double effectiveHuntingArea = totalHuntingArea + 100.0;
        section.setEffectiveHuntingArea(effectiveHuntingArea);

        assertValidationErrors(validate(section),
                effectiveHuntingAreaLargerThanTotalHuntingArea(effectiveHuntingArea, totalHuntingArea));
    }

    @Test
    public void testValidate_whenMoosesRemainingInEffectiveAreaGreaterThanMoosesRemainingInTotalArea() {
        final MooseDataCardSection_8_1 section = newSection81();
        final int totalRemaining = section.getMoosesRemainingInTotalHuntingArea();
        final int effectiveRemaining = totalRemaining + 20;
        section.setMoosesRemainingInEffectiveHuntingArea(effectiveRemaining);

        assertValidationErrors(validate(section),
                moosesRemainingInEffectiveHuntingAreaGreaterThanMoosesRemainingInTotalHuntingArea(
                        effectiveRemaining, totalRemaining));
    }

    @Test
    public void testValidate_whenHuntingAreaNotGivenAtAll() {
        final MooseDataCardSection_8_1 section = newSection81()
                .withTotalHuntingArea(null)
                .withEffectiveHuntingArea(null);

        assertValidationErrors(validate(section),
                huntingAreaNotGivenAtAll(),
                moosesRemainingInTotalHuntingAreaGivenButAreaMissing(),
                moosesRemainingInEffectiveHuntingAreaGivenButAreaMissing());
    }

    @Test
    public void testValidate_whenMoosesRemainingNotGivenAtAll() {
        final MooseDataCardSection_8_1 section = newSection81()
                .withMoosesRemainingInTotalHuntingArea(null)
                .withMoosesRemainingInEffectiveHuntingArea(null);

        assertValidationErrors(validate(section), moosesRemainingNotGivenAtAll());
    }

    @Test
    public void testValidate_whenHuntingAreaNotGivenAtAll_andMoosesRemainingNotGivenAtAll() {
        final MooseDataCardSection_8_1 section = new MooseDataCardSection_8_1();

        assertValidationErrors(validate(section), huntingAreaNotGivenAtAll(), moosesRemainingNotGivenAtAll());
    }

    @Test
    public void testValidate_whenTotalHuntingAreaNotGiven_andMoosesRemainingGivenOnlyForTotalArea() {
        final MooseDataCardSection_8_1 section = newSection81()
                .withTotalHuntingArea(null)
                .withMoosesRemainingInEffectiveHuntingArea(null);

        assertValidationErrors(validate(section), moosesRemainingInTotalHuntingAreaGivenButAreaMissing());
    }

    @Test
    public void testValidate_whenEffectiveHuntingAreaNotGiven_andMoosesRemainingGivenOnlyForEffectiveArea() {
        final MooseDataCardSection_8_1 section = newSection81()
                .withEffectiveHuntingArea(null)
                .withEffectiveHuntingAreaPercentage(null)
                .withMoosesRemainingInTotalHuntingArea(null);

        assertValidationErrors(validate(section), moosesRemainingInEffectiveHuntingAreaGivenButAreaMissing());
    }

    @Test
    public void testValidate_whenTotalHuntingAreaNotGiven_andEffectiveHuntingAreaGivenAsPercentageShare() {
        final MooseDataCardSection_8_1 section = newSection81()
                .withTotalHuntingArea(null)
                .withEffectiveHuntingArea(null)
                .withEffectiveHuntingAreaPercentage(50.0);

        assertValidationErrors(validate(section),
                totalHuntingAreaMissingAndEffectiveHuntingAreaGivenAsPercentageShare(),
                moosesRemainingInTotalHuntingAreaGivenButAreaMissing());
    }

    @Test
    public void testValidate_whenTotalHuntingAreaNotGiven_andEffectiveHuntingAreaGivenAsPercentageShare_andMoosesRemainingNotGivenAtAll() {
        final MooseDataCardSection_8_1 section = new MooseDataCardSection_8_1()
                .withEffectiveHuntingAreaPercentage(50.0);

        assertValidationErrors(validate(section),
                totalHuntingAreaMissingAndEffectiveHuntingAreaGivenAsPercentageShare(),
                moosesRemainingNotGivenAtAll());
    }

    private static <N extends Number & Comparable<N>> void testValidate_withFieldHavingIllegalValue(
            final MooseDataCardSection_8_1 section,
            final MooseDataCardSummaryField<MooseDataCardSection_8_1, N> field) {

        assertNumericFieldValidationError(section, MooseDataCardSection81Validator::validate, field);
    }
}
