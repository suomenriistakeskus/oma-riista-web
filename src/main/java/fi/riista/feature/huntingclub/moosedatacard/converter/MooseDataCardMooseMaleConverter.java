package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardMooseMaleValidator;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import io.vavr.Tuple2;

import javax.annotation.Nonnull;

public class MooseDataCardMooseMaleConverter extends MooseDataCardHarvestConverter<MooseDataCardMooseMale> {

    public MooseDataCardMooseMaleConverter(@Nonnull final HarvestPermitSpeciesAmount mooseSpeciesAmount,
                                           @Nonnull final Person contactPerson,
                                           @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardMooseMaleValidator(mooseSpeciesAmount, defaultCoordinates),
                mooseSpeciesAmount.getGameSpecies(),
                contactPerson);
    }

    @Override
    protected Tuple2<Harvest, HarvestSpecimen> convert(@Nonnull final MooseDataCardMooseMale validSource) {
        return super.convert(validSource).map2(specimen -> {

            specimen.setAntlerPointsLeft(validSource.getAntlerPointsLeft());
            specimen.setAntlerPointsRight(validSource.getAntlerPointsRight());
            specimen.setAntlersWidth(validSource.getAntlersWidth());
            specimen.setAntlersType(
                    HasMooseDataCardEncoding.getEnumOrNull(GameAntlersType.class, validSource.getAntlersType()));

            return specimen;
        });
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
