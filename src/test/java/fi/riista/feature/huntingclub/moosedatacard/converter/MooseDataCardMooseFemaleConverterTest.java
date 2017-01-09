package fi.riista.feature.huntingclub.moosedatacard.converter;

import static org.junit.Assert.assertEquals;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;

import org.junit.Test;

import javax.annotation.Nonnull;

public class MooseDataCardMooseFemaleConverterTest extends MooseDataCardHarvestConverterTest<MooseDataCardMooseFemale> {

    @Override
    protected MooseDataCardMooseFemale newHarvestSource() {
        return MooseDataCardObjectFactory.newMooseFemale();
    }

    @Override
    protected MooseDataCardMooseFemaleConverter newConverter(
            @Nonnull final GameSpecies mooseSpecies,
            @Nonnull final Person person,
            @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseFemaleConverter(mooseSpecies, person, defaultCoordinates);
    }

    @Test
    public void testAgeAndGender() {
        testConversion(newHarvestSource(), harvest -> {}, specimen -> {
            assertEquals(GameAge.ADULT, specimen.getAge());
            assertEquals(GameGender.FEMALE, specimen.getGender());
        });
    }

}
