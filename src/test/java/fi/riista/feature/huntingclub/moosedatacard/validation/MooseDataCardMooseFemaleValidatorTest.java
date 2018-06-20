package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MooseDataCardMooseFemaleValidatorTest extends MooseDataCardHarvestValidatorTest<MooseDataCardMooseFemale> {

    @Override
    protected MooseDataCardMooseFemaleValidator getValidator(@Nonnull final Has2BeginEndDates season,
                                                             @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseFemaleValidator(season, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseFemale newHarvest(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseFemale(date);
    }
}
