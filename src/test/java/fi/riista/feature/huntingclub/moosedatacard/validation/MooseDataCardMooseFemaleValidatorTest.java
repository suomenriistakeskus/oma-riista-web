package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;

import javax.annotation.Nonnull;

public class MooseDataCardMooseFemaleValidatorTest extends MooseDataCardHarvestValidatorTest<MooseDataCardMooseFemale> {

    @Override
    protected MooseDataCardMooseFemaleValidator getValidator(
            @Nonnull final Has2BeginEndDates permitSeason, @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseFemaleValidator(permitSeason, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseFemale newHarvest() {
        return MooseDataCardObjectFactory.newMooseFemale();
    }

}
