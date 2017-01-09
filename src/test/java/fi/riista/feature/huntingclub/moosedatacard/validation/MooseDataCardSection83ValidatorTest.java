package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newSection83;
import static fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection83Validator.validate;
import static org.junit.Assert.assertTrue;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.util.Asserts;

import org.junit.Test;

import java.util.stream.Stream;

public class MooseDataCardSection83ValidatorTest {

    @Test
    public void testValidate_withValidData() {
        assertTrue(validate(newSection83()).isValid());
    }

    @Test
    public void testValidate_withEmptyData() {
        assertTrue(validate(new MooseDataCardSection_8_3()).isValid());
    }

    @Test
    public void testValidate_withOutOfRangeNumericValues() {
        final int illegal = -1;

        final MooseDataCardSection_8_3 section = new MooseDataCardSection_8_3()
                .withNumberOfDrownedMooses(illegal)
                .withNumberOfMoosesKilledByBear(illegal)
                .withNumberOfMoosesKilledByWolf(illegal)
                .withNumberOfMoosesKilledInTrafficAccident(illegal)
                .withNumberOfMoosesKilledInPoaching(illegal)
                .withNumberOfMoosesKilledInRutFight(illegal)
                .withNumberOfStarvedMooses(illegal)
                .withNumberOfMoosesDeceasedByOtherReason(illegal);

        Asserts.assertNumericFieldValidationErrors(section, MooseDataCardSection83Validator::validate, Stream.of(
                MooseDataCardSummaryField.DROWNED_AMOUNT,
                MooseDataCardSummaryField.KILLED_BY_BEAR_AMOUNT,
                MooseDataCardSummaryField.KILLED_BY_WOLF_AMOUNT,
                MooseDataCardSummaryField.KILLED_IN_TRAFFIC_ACCIDENT_AMOUNT,
                MooseDataCardSummaryField.KILLED_IN_POACHING_AMOUNT,
                MooseDataCardSummaryField.KILLED_IN_RUT_FIGHT_AMOUNT,
                MooseDataCardSummaryField.STARVED_AMOUNT,
                MooseDataCardSummaryField.DECEASED_BY_OTHER_REASON_AMOUNT));
    }

}
