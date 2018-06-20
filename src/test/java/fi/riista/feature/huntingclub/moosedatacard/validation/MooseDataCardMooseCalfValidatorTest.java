package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.genderOfMooseCalfContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.mooseCalfMissingGender;
import static fi.riista.test.Asserts.assertValid;
import static fi.riista.test.Asserts.assertValidationErrors;
import static org.junit.Assert.assertEquals;

public class MooseDataCardMooseCalfValidatorTest extends MooseDataCardHarvestValidatorTest<MooseDataCardMooseCalf> {

    @Override
    protected MooseDataCardMooseCalfValidator getValidator(@Nonnull final Has2BeginEndDates season,
                                                           @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseCalfValidator(season, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseCalf newHarvest(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseCalf(date);
    }

    @Test
    public void testValidGender() {
        final MooseDataCardMooseCalf input = newHarvestWithinSeason();

        assertValid(validate(input), output -> assertEquals(input.getGender(), output.getGender()));
    }

    @Test
    public void testMissingGender() {
        final MooseDataCardMooseCalf input = newHarvestWithinSeason().withGender(null);

        assertValidationErrors(validate(input), mooseCalfMissingGender(input));
    }

    @Test
    public void testInvalidGender() {
        final MooseDataCardMooseCalf input = newHarvestWithinSeason().withGender("invalid");

        assertValidationErrors(validate(input), genderOfMooseCalfContainsIllegalCharacters(input));
    }
}
