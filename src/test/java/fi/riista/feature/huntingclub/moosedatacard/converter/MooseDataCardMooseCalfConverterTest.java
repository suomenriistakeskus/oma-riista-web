package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;

public class MooseDataCardMooseCalfConverterTest extends MooseDataCardHarvestConverterTest<MooseDataCardMooseCalf> {

    @Override
    protected MooseDataCardMooseCalfConverter newConverter(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                           @Nonnull final Person person,
                                                           @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseCalfConverter(speciesAmount, person, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseCalf newHarvestSource(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseCalf(date);
    }

    @Test
    public void testValidFields() {
        final MooseDataCardMooseCalf calf = newHarvestSourceWithinSeason();

        testConversion(calf, harvest -> {}, specimen -> {
            assertEquals(GameAge.YOUNG, specimen.getAge());
            assertEquals(HasMooseDataCardEncoding.getEnum(GameGender.class, calf.getGender()), specimen.getGender());
            assertEquals(new Boolean(calf.isAlone()), specimen.getAlone());
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidGender() {
        testConversion(newHarvestSourceWithinSeason().withGender("invalid"), harvest -> {}, specimen -> {});
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingGender() {
        testConversion(newHarvestSourceWithinSeason().withGender(null), harvest -> {}, specimen -> {});
    }
}
