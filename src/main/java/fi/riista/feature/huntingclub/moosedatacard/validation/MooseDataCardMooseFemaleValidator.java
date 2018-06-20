package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import io.vavr.control.Validation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class MooseDataCardMooseFemaleValidator extends MooseDataCardHarvestValidator<MooseDataCardMooseFemale> {

    public MooseDataCardMooseFemaleValidator(@Nonnull final Has2BeginEndDates permitSeason,
                                             @Nonnull final GeoLocation defaultCoordinates) {

        super(permitSeason, defaultCoordinates);
    }

    @Override
    public Validation<List<String>, MooseDataCardMooseFemale> validate(final MooseDataCardMooseFemale harvest) {
        return getCommonHarvestValidation(harvest).mapError(Collections::singletonList);
    }
}
