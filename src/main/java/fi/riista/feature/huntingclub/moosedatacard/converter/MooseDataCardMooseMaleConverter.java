package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardMooseMaleValidator;

import javaslang.Tuple2;

import javax.annotation.Nonnull;

public class MooseDataCardMooseMaleConverter extends MooseDataCardHarvestConverter<MooseDataCardMooseMale> {

    public MooseDataCardMooseMaleConverter(
            @Nonnull final GameSpecies mooseSpecies,
            @Nonnull final Person contactPerson,
            @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardMooseMaleValidator(defaultCoordinates), mooseSpecies, contactPerson);
    }

    @Override
    protected Tuple2<Harvest, HarvestSpecimen> convert(@Nonnull final MooseDataCardMooseMale validSource) {
        final Tuple2<Harvest, HarvestSpecimen> result = super.convert(validSource);

        final HarvestSpecimen specimen = result._2;
        specimen.setAntlerPointsLeft(validSource.getAntlerPointsLeft());
        specimen.setAntlerPointsRight(validSource.getAntlerPointsRight());
        specimen.setAntlersWidth(validSource.getAntlersWidth());

        specimen.setAntlersType(HasMooseDataCardEncoding
                .enumOf(GameAntlersType.class, validSource.getAntlersType())
                .getOrElseGet(invalid -> null));

        return result;
    }

    @Override
    protected GameAge getAge(final MooseDataCardMooseMale male) {
        return GameAge.ADULT;
    }

    @Override
    protected GameGender getGender(final MooseDataCardMooseMale male) {
        return GameGender.MALE;
    }

}
