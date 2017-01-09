package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardMooseFemaleValidator;

import javax.annotation.Nonnull;

public class MooseDataCardMooseFemaleConverter extends MooseDataCardHarvestConverter<MooseDataCardMooseFemale> {

    public MooseDataCardMooseFemaleConverter(
            @Nonnull final GameSpecies mooseSpecies,
            @Nonnull final Person contactPerson,
            @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardMooseFemaleValidator(defaultCoordinates), mooseSpecies, contactPerson);
    }

    @Override
    protected GameAge getAge(final MooseDataCardMooseFemale female) {
        return GameAge.ADULT;
    }

    @Override
    protected GameGender getGender(final MooseDataCardMooseFemale female) {
        return GameGender.FEMALE;
    }

}
