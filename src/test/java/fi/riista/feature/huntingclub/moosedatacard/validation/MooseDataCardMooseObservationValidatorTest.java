package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSeenMoosesOfObservationIsNotGreaterThanZero;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseObservation;
import static org.junit.Assert.assertEquals;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;

import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.stream.Stream;

public class MooseDataCardMooseObservationValidatorTest
        extends MooseDataCardObservationValidatorTestBase<MooseDataCardObservation> {

    @Override
    protected MooseDataCardMooseObservationValidator getValidator(@Nonnull final GeoLocation defaultCoordinates) {
        return new MooseDataCardMooseObservationValidator(defaultCoordinates);
    }

    @Override
    protected MooseDataCardObservation newObservation() {
        return newMooseObservation();
    }

    @Test
    public void testWhenAllSpeciesAmountsAreGreaterThanZero() {
        final MooseDataCardObservation input = newObservation();

        assertAccepted(input, output -> {
            assertEquals(input.getDate(), output.getDate());
            assertEquals(input.getGeoLocation(), output.getGeoLocation());
            assertEquals(input.getAU(), output.getAU());
            assertEquals(input.getN0(), output.getN0());
            assertEquals(input.getN1(), output.getN1());
            assertEquals(input.getN2(), output.getN2());
            assertEquals(input.getN3(), output.getN3());
            assertEquals(input.getT(), output.getT());
        });
    }

    @Test
    public void testWhenNoAmountIsGreaterThanZero() {
        Stream.of(0, -1, null)
                .map(n -> newObservation().withAU(n).withN0(n).withN1(n).withN2(n).withN3(n).withT(n))
                .forEach(obs -> assertAbandonReason(obs, sumOfSeenMoosesOfObservationIsNotGreaterThanZero(obs)));
    }

}
