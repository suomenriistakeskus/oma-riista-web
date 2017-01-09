package fi.riista.feature.huntingclub.moosedatacard.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.util.Asserts;

import org.junit.Test;

import javax.annotation.Nonnull;

public class MooseDataCardMooseMaleValidatorTest extends MooseDataCardHarvestValidatorTest<MooseDataCardMooseMale> {

    @Override
    protected MooseDataCardMooseMaleValidator getValidator(
            @Nonnull final Has2BeginEndDates permitSeason, @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseMaleValidator(permitSeason, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseMale newHarvest() {
        return MooseDataCardObjectFactory.newMooseMale();
    }

    @Test
    public void testValidMaleSpecificFields() {
        final MooseDataCardMooseMale input = newHarvest();

        Asserts.assertValid(validate(input, newSeason()), output -> {
            assertEquals(input.getAntlersType(), output.getAntlersType());
            assertEquals(input.getAntlerPointsLeft(), output.getAntlerPointsLeft());
            assertEquals(input.getAntlerPointsRight(), output.getAntlerPointsRight());
            assertEquals(input.getAntlersWidth(), output.getAntlersWidth());
        });
    }

    @Test
    public void testIllegalMaleSpecificFields() {
        final MooseDataCardMooseMale input = newHarvest()
                .withAntlersType("invalid")
                .withAntlersWidth(Integer.MAX_VALUE)
                .withAntlerPointsLeft(Integer.MAX_VALUE)
                .withAntlerPointsRight(Integer.MAX_VALUE);

        Asserts.assertValid(validate(input, newSeason()), output -> {
            assertNull(output.getAntlersType());
            assertNull(output.getAntlersWidth());
            assertNull(output.getAntlerPointsLeft());
            assertNull(output.getAntlerPointsRight());
        });
    }

    @Test
    public void testMaleSpecificFieldsWhenNull() {
        final MooseDataCardMooseMale input = newHarvest()
                .withAntlersType(null)
                .withAntlersWidth(null)
                .withAntlerPointsLeft(null)
                .withAntlerPointsRight(null);

        Asserts.assertValid(validate(input, newSeason()), output -> {
            assertNull(output.getAntlersType());
            assertNull(output.getAntlersWidth());
            assertNull(output.getAntlerPointsLeft());
            assertNull(output.getAntlerPointsRight());
        });
    }

}
