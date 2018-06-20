package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSeenMoosesOfObservationIsNotGreaterThanZero;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseObservation;
import static org.junit.Assert.assertEquals;

public class MooseDataCardMooseObservationValidatorTest
        extends MooseDataCardObservationValidatorTestBase<MooseDataCardObservation> {

    @Override
    protected MooseDataCardMooseObservationValidator getValidator(final int huntingYear,
                                                                  @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseObservationValidator(huntingYear, defaultCoordinates);
    }

    @Override
    protected MooseDataCardObservation newObservation(@Nullable final LocalDate date) {
        return newMooseObservation(date);
    }

    @Test
    public void testWhenAllSpeciesAmountsAreGreaterThanZero() {
        final MooseDataCardObservation input = newObservationWithinSeason();
        input.setGeoLocation(DEFAULT_COORDINATES);

        assertAccepted(input, output -> {
            assertEquals(input.getDate(), output.getDate());
            assertEquals(DEFAULT_COORDINATES, output.getGeoLocation());
            assertEquals(input.getAU(), output.getAU());
            assertEquals(input.getN0(), output.getN0());
            assertEquals(input.getN1(), output.getN1());
            assertEquals(input.getN2(), output.getN2());
            assertEquals(input.getN3(), output.getN3());
            assertEquals(input.getY(), output.getY());
            assertEquals(input.getT(), output.getT());
        });
    }

    @Test
    public void testWhenNoAmountIsGreaterThanZero() {
        Stream.of(0, -1, null)
                .map(n -> newObservationWithinSeason()
                        .withAU(n).withN0(n).withN1(n).withN2(n).withN3(n).withY(n).withT(n))
                .forEach(obs -> assertAbandonReason(obs, sumOfSeenMoosesOfObservationIsNotGreaterThanZero(obs)));
    }
}
