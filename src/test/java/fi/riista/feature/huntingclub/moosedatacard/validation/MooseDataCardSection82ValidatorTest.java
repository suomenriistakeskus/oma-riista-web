package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;
import fi.riista.test.Asserts;
import org.junit.Test;

import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newHarvestAmounts;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newSection82;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.adultFemaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.adultMaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.nonEdibleAdultHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.nonEdibleYoungHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.youngFemaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.youngMaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection82Validator.validate;
import static fi.riista.test.Asserts.assertNumericFieldValidationErrors;
import static fi.riista.test.Asserts.assertValidationErrors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MooseDataCardSection82ValidatorTest {

    @Test
    public void testValidate_withValidData() {
        final MooseDataCardSection_8_2 section = newSection82();

        Asserts.assertValid(validate(section, newHarvestAmounts(section)), s -> {
            assertEquals(section.toString(), s.toString());
        });
    }

    @Test
    public void testValidate_withEmptyData() {
        assertTrue(validate(new MooseDataCardSection_8_2(), newHarvestAmounts()).isValid());
    }

    @Test
    public void testValidate_withOutOfRangeNumericValues() {
        final Function<Integer, Integer> illegalMutation = n -> -1;

        final MooseDataCardCalculatedHarvestAmounts harvestAmounts = newHarvestAmounts().asTuple6()
                .map1(illegalMutation)
                .map2(illegalMutation)
                .map3(illegalMutation)
                .map4(illegalMutation)
                .map5(illegalMutation)
                .map6(illegalMutation)
                .apply(MooseDataCardCalculatedHarvestAmounts::new);

        assertNumericFieldValidationErrors(newSection82(harvestAmounts), s -> validate(s, harvestAmounts), Stream.of(
                MooseDataCardSummaryField.ADULT_MALE_AMOUNT,
                MooseDataCardSummaryField.ADULT_FEMALE_AMOUNT,
                MooseDataCardSummaryField.YOUNG_MALE_AMOUNT,
                MooseDataCardSummaryField.YOUNG_FEMALE_AMOUNT,
                MooseDataCardSummaryField.NON_EDIBLE_ADULT_AMOUNT,
                MooseDataCardSummaryField.NON_EDIBLE_YOUNG_AMOUNT));
    }

    @Test
    public void testValidate_withMultipleHarvestAmountMismatches() {
        final Function<Integer, Integer> mutation = n -> Integer.MAX_VALUE;

        final MooseDataCardSection_8_2 section = newSection82();
        final MooseDataCardCalculatedHarvestAmounts harvestAmounts = newHarvestAmounts(section).asTuple6()
                .map1(mutation)
                .map2(mutation)
                .map3(mutation)
                .map4(mutation)
                .map5(mutation)
                .map6(mutation)
                .apply(MooseDataCardCalculatedHarvestAmounts::new);

        assertValidationErrors(validate(section, harvestAmounts),
                adultMaleHarvestCountMismatch(section.getNumberOfAdultMales(), harvestAmounts.numberOfAdultMales),
                adultFemaleHarvestCountMismatch(section.getNumberOfAdultFemales(), harvestAmounts.numberOfAdultFemales),
                youngMaleHarvestCountMismatch(section.getNumberOfYoungMales(), harvestAmounts.numberOfYoungMales),
                youngFemaleHarvestCountMismatch(section.getNumberOfYoungFemales(), harvestAmounts.numberOfYoungFemales),
                nonEdibleAdultHarvestCountMismatch(
                        section.getTotalNumberOfNonEdibleAdults(), harvestAmounts.totalNumberOfNonEdibleAdults),
                nonEdibleYoungHarvestCountMismatch(
                        section.getTotalNumberOfNonEdibleYoungs(), harvestAmounts.totalNumberOfNonEdibleYoungs));
    }
}
