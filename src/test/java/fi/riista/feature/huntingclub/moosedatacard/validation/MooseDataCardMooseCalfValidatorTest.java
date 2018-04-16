package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.genderOfMooseCalfContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.mooseCalfMissingGender;
import static org.junit.Assert.assertEquals;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.util.Asserts;

import org.junit.Test;

import javax.annotation.Nonnull;

public class MooseDataCardMooseCalfValidatorTest extends MooseDataCardHarvestValidatorTest<MooseDataCardMooseCalf> {

    @Override
    protected MooseDataCardMooseCalfValidator getValidator(@Nonnull final Has2BeginEndDates permitSeason,
                                                           @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseCalfValidator(permitSeason, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseCalf newHarvest() {
        return MooseDataCardObjectFactory.newMooseCalf();
    }

    @Test
    public void testValidGender() {
        final MooseDataCardMooseCalf input = newHarvest();

        Asserts.assertValid(validate(input, newSeason()), output -> {
            assertEquals(input.getGender(), output.getGender());
        });
    }

    @Test
    public void testMissingGender() {
        final MooseDataCardMooseCalf input = newHarvest().withGender(null);
        Asserts.assertValidationErrors(validate(input, newSeason()), mooseCalfMissingGender(input));
    }

    @Test
    public void testInvalidGender() {
        final MooseDataCardMooseCalf input = newHarvest().withGender("invalid");
        Asserts.assertValidationErrors(validate(input, newSeason()), genderOfMooseCalfContainsIllegalCharacters(input));
    }

}
