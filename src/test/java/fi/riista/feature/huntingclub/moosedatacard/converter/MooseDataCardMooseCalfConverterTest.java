package fi.riista.feature.huntingclub.moosedatacard.converter;

import static org.junit.Assert.assertEquals;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;

import org.junit.Test;

import javax.annotation.Nonnull;

public class MooseDataCardMooseCalfConverterTest extends MooseDataCardHarvestConverterTest<MooseDataCardMooseCalf> {

    @Override
    protected MooseDataCardMooseCalf newHarvestSource() {
        return MooseDataCardObjectFactory.newMooseCalf();
    }

    @Override
    protected MooseDataCardMooseCalfConverter newConverter(
            @Nonnull final GameSpecies mooseSpecies,
            @Nonnull final Person person,
            @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseCalfConverter(mooseSpecies, person, defaultCoordinates);
    }

    @Test
    public void testValidGender() {
        final MooseDataCardMooseCalf calf = newHarvestSource();

        testConversion(calf, harvest -> {}, specimen -> {
            assertEquals(GameAge.YOUNG, specimen.getAge());
            assertEquals(
                    HasMooseDataCardEncoding.enumOf(GameGender.class, calf.getGender())
                            .getOrElseThrow(invalid -> new IllegalStateException("Could not convert gender")),
                    specimen.getGender());
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidGender() {
        testConversion(newHarvestSource().withGender("invalid"), harvest -> {}, specimen -> {});
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingGender() {
        testConversion(newHarvestSource().withGender(null), harvest -> {}, specimen -> {});
    }

}
