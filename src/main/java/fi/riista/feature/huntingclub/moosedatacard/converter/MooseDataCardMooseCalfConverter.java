package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardMooseCalfValidator;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import io.vavr.Tuple2;

import javax.annotation.Nonnull;

public class MooseDataCardMooseCalfConverter extends MooseDataCardHarvestConverter<MooseDataCardMooseCalf> {

    public MooseDataCardMooseCalfConverter(@Nonnull final HarvestPermitSpeciesAmount mooseSpeciesAmount,
                                           @Nonnull final Person contactPerson,
                                           @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardMooseCalfValidator(mooseSpeciesAmount, defaultCoordinates),
                mooseSpeciesAmount.getGameSpecies(),
                contactPerson);
    }

    @Override
    protected Tuple2<Harvest, HarvestSpecimen> convert(@Nonnull final MooseDataCardMooseCalf validSource) {
        return super.convert(validSource).map2(specimen -> {
            specimen.setAlone(validSource.isAlone());
            return specimen;
        });
    }

    @Override
    protected GameAge getAge(final MooseDataCardMooseCalf harvest) {
        return GameAge.YOUNG;
    }

    @Override
    protected GameGender getGender(final MooseDataCardMooseCalf mooseCalf) {
        return HasMooseDataCardEncoding.getEnumOrThrow(GameGender.class, mooseCalf.getGender(), invalid -> {
            return new IllegalStateException("Invalid gender for moose calf should not have passed validation: "
                    + invalid.map(s -> '"' + s + '"').orElse("null"));
        });
    }
}
