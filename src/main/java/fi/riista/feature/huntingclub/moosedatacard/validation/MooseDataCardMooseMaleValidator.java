package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.util.F;

import javaslang.control.Validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public class MooseDataCardMooseMaleValidator extends MooseDataCardHarvestValidator<MooseDataCardMooseMale> {

    public MooseDataCardMooseMaleValidator(@Nonnull final GeoLocation defaultCoordinates) {
        super(defaultCoordinates);
    }

    public MooseDataCardMooseMaleValidator(
            @Nonnull final Has2BeginEndDates permitSeason, @Nonnull final GeoLocation defaultCoordinates) {

        super(permitSeason, defaultCoordinates);
    }

    @Override
    public Validation<List<String>, MooseDataCardMooseMale> validate(@Nonnull final MooseDataCardMooseMale input) {
        return getCommonHarvestValidation(input)
                .map(inputWithValidCommonFields -> {
                    inputWithValidCommonFields.setAntlersType(getValidAntlersType(input));
                    inputWithValidCommonFields.setAntlersWidth(
                            MooseDataCardDiaryEntryField.ANTLERS_WIDTH.getValidOrNull(input));
                    inputWithValidCommonFields.setAntlerPointsLeft(
                            MooseDataCardDiaryEntryField.ANTLER_POINTS_LEFT.getValidOrNull(input));
                    inputWithValidCommonFields.setAntlerPointsRight(
                            MooseDataCardDiaryEntryField.ANTLER_POINTS_RIGHT.getValidOrNull(input));
                    return inputWithValidCommonFields;
                })
                .leftMap(Collections::singletonList);
    }

    @Nullable
    protected String getValidAntlersType(@Nonnull final MooseDataCardMooseMale male) {
        return F.trimToOptional(male.getAntlersType())
                .map(antlersType -> {
                    switch (antlersType) {
                        case "H":
                            return GameAntlersType.HANKO;
                        case "L":
                            return GameAntlersType.LAPIO;
                        case "S":
                            return GameAntlersType.SEKA;
                        default:
                            return null;
                    }
                })
                .map(HasMooseDataCardEncoding::getMooseDataCardEncoding)
                .orElse(null);
    }

}
