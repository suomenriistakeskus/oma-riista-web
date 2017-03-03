package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.genderOfMooseCalfContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.mooseCalfMissingGender;
import static fi.riista.util.Functions.firstOf2;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import javaslang.Value;
import javaslang.control.Validation;

import javax.annotation.Nonnull;
import java.util.List;

public class MooseDataCardMooseCalfValidator extends MooseDataCardHarvestValidator<MooseDataCardMooseCalf> {

    public MooseDataCardMooseCalfValidator(@Nonnull final GeoLocation defaultCoordinates) {
        super(defaultCoordinates);
    }

    public MooseDataCardMooseCalfValidator(@Nonnull final Has2BeginEndDates permitSeason,
                                           @Nonnull final GeoLocation defaultCoordinates) {

        super(permitSeason, defaultCoordinates);
    }

    @Override
    public Validation<List<String>, MooseDataCardMooseCalf> validate(@Nonnull final MooseDataCardMooseCalf input) {
        return getCommonHarvestValidation(input)
                .combine(validateGender(input))
                .ap(firstOf2())
                .leftMap(Value::toJavaList);
    }

    private static Validation<String, GameGender> validateGender(@Nonnull final MooseDataCardMooseCalf calf) {
        return Validation.fromEither(HasMooseDataCardEncoding.eitherInvalidOrValid(GameGender.class, calf.getGender()))
                .leftMap(invalidOpt -> invalidOpt.isPresent()
                    ? genderOfMooseCalfContainsIllegalCharacters(calf)
                    : mooseCalfMissingGender(calf));
    }

}
