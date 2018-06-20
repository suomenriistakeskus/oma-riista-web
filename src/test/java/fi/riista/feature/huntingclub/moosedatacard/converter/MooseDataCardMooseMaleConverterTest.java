package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MooseDataCardMooseMaleConverterTest extends MooseDataCardHarvestConverterTest<MooseDataCardMooseMale> {

    @Override
    protected MooseDataCardMooseMaleConverter newConverter(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                           @Nonnull final Person person,
                                                           @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseMaleConverter(speciesAmount, person, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseMale newHarvestSource(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseMale(date);
    }

    @Test
    public void testValidMaleSpecificFields() {
        final MooseDataCardMooseMale male = newHarvestSourceWithinSeason();

        testConversion(male, harvest -> {}, specimen -> {
            assertEquals(GameAge.ADULT, specimen.getAge());
            assertEquals(GameGender.MALE, specimen.getGender());
            assertEquals(
                    HasMooseDataCardEncoding.getEnum(GameAntlersType.class, male.getAntlersType()),
                    specimen.getAntlersType());
            assertEquals(male.getAntlersWidth(), specimen.getAntlersWidth());
            assertEquals(male.getAntlerPointsLeft(), specimen.getAntlerPointsLeft());
            assertEquals(male.getAntlerPointsRight(), specimen.getAntlerPointsRight());
        });
    }

    @Test
    public void testInvalidMaleSpecificFields() {
        final MooseDataCardMooseMale male = newHarvestSourceWithinSeason()
                .withAntlersType("invalid")
                .withAntlerPointsLeft(Integer.MAX_VALUE)
                .withAntlerPointsRight(Integer.MAX_VALUE)
                .withAntlersWidth(Integer.MAX_VALUE);

        testConversion(male, harvest -> {}, specimen -> {
            assertNull(specimen.getAntlersType());
            assertNull(specimen.getAntlersWidth());
            assertNull(specimen.getAntlerPointsLeft());
            assertNull(specimen.getAntlerPointsRight());
        });
    }

    @Test
    public void testMissingMaleSpecificFields() {
        final MooseDataCardMooseMale male = newHarvestSourceWithinSeason()
                .withAntlersType(null)
                .withAntlerPointsLeft(null)
                .withAntlerPointsRight(null)
                .withAntlersWidth(null);

        testConversion(male, harvest -> {}, specimen -> {
            assertNull(specimen.getAntlersType());
            assertNull(specimen.getAntlersWidth());
            assertNull(specimen.getAntlerPointsLeft());
            assertNull(specimen.getAntlerPointsRight());
        });
    }
}
