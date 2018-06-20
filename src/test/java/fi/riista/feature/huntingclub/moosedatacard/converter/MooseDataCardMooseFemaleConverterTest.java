package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;

public class MooseDataCardMooseFemaleConverterTest extends MooseDataCardHarvestConverterTest<MooseDataCardMooseFemale> {

    @Override
    protected MooseDataCardMooseFemaleConverter newConverter(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                             @Nonnull final Person person,
                                                             @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseFemaleConverter(speciesAmount, person, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseFemale newHarvestSource(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseFemale(date);
    }

    @Test
    public void testAgeAndGender() {
        testConversion(newHarvestSourceWithinSeason(), harvest -> {}, specimen -> {
            assertEquals(GameAge.ADULT, specimen.getAge());
            assertEquals(GameGender.FEMALE, specimen.getGender());
        });
    }
}
