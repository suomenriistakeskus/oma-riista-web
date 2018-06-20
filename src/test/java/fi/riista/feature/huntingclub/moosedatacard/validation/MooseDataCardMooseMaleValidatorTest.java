package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fi.riista.test.Asserts.assertValid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MooseDataCardMooseMaleValidatorTest extends MooseDataCardHarvestValidatorTest<MooseDataCardMooseMale> {

    @Override
    protected MooseDataCardMooseMaleValidator getValidator(@Nonnull final Has2BeginEndDates season,
                                                           @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseMaleValidator(season, defaultCoordinates);
    }

    @Override
    protected MooseDataCardMooseMale newHarvest(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseMale(date);
    }

    @Test
    public void testValidMaleSpecificFields() {
        final MooseDataCardMooseMale input = newHarvestWithinSeason();

        assertValid(validate(input), output -> {
            assertEquals(input.getAntlersType(), output.getAntlersType());
            assertEquals(input.getAntlerPointsLeft(), output.getAntlerPointsLeft());
            assertEquals(input.getAntlerPointsRight(), output.getAntlerPointsRight());
            assertEquals(input.getAntlersWidth(), output.getAntlersWidth());
        });
    }

    @Test
    public void testIllegalMaleSpecificFields() {
        final MooseDataCardMooseMale input = newHarvestWithinSeason()
                .withAntlersType("invalid")
                .withAntlersWidth(Integer.MAX_VALUE)
                .withAntlerPointsLeft(Integer.MAX_VALUE)
                .withAntlerPointsRight(Integer.MAX_VALUE);

        assertValid(validate(input), output -> {
            assertNull(output.getAntlersType());
            assertNull(output.getAntlersWidth());
            assertNull(output.getAntlerPointsLeft());
            assertNull(output.getAntlerPointsRight());
        });
    }

    @Test
    public void testMaleSpecificFieldsWhenNull() {
        final MooseDataCardMooseMale input = newHarvestWithinSeason()
                .withAntlersType(null)
                .withAntlersWidth(null)
                .withAntlerPointsLeft(null)
                .withAntlerPointsRight(null);

        assertValid(validate(input), output -> {
            assertNull(output.getAntlersType());
            assertNull(output.getAntlersWidth());
            assertNull(output.getAntlerPointsLeft());
            assertNull(output.getAntlerPointsRight());
        });
    }
}
