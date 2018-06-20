package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.largeCarnivoreMissingObservationType;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationTypeOfLargeCarnivoreContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newLargeCarnivoreObservation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MooseDataCardLargeCarnivoreObservationValidatorTest
        extends MooseDataCardObservationValidatorTestBase<MooseDataCardLargeCarnivoreObservation> {

    @Override
    protected MooseDataCardLargeCarnivoreObservationValidator getValidator(final int huntingYear,
                                                                           @Nonnull final GeoLocation defaultCoordinates) {

        return new MooseDataCardLargeCarnivoreObservationValidator(huntingYear, defaultCoordinates);
    }

    @Override
    protected MooseDataCardLargeCarnivoreObservation newObservation(@Nullable final LocalDate date) {
        return newLargeCarnivoreObservation(date);
    }

    @Test
    public void testWhenAllSpeciesAmountsGreaterThanZero() {
        final MooseDataCardLargeCarnivoreObservation input = newObservationWithinSeason();
        input.setGeoLocation(DEFAULT_COORDINATES);

        assertAccepted(input, output -> {
            assertEquals(input.getDate(), output.getDate());
            assertEquals(DEFAULT_COORDINATES, output.getGeoLocation());
            assertEquals(input.getObservationType(), output.getObservationType());
            assertEquals(input.getAdditionalInfo(), output.getAdditionalInfo());
            assertEquals(input.getNumberOfWolves(), output.getNumberOfWolves());
            assertEquals(input.getNumberOfBears(), output.getNumberOfBears());
            assertEquals(input.getNumberOfLynxes(), output.getNumberOfLynxes());
            assertEquals(input.getNumberOfWolverines(), output.getNumberOfWolverines());
        });
    }

    @Test
    public void testWhenOnlyOneAmountIsGreaterThanZero() {
        final MooseDataCardLargeCarnivoreObservation input = newObservationWithinSeason()
                .withNumberOfWolves(0)
                .withNumberOfBears(0)
                .withNumberOfLynxes(0);

        assertAccepted(input, output -> {
            assertNull(output.getNumberOfWolves());
            assertNull(output.getNumberOfBears());
            assertNull(output.getNumberOfLynxes());
            assertEquals(input.getNumberOfWolverines(), output.getNumberOfWolverines());
        });
    }

    @Test
    public void testWhenObservationTypeIsMissing() {
        final MooseDataCardLargeCarnivoreObservation input = newObservationWithinSeason().withObservationType(null);
        assertAbandonReason(input, largeCarnivoreMissingObservationType(input));
    }

    @Test
    public void testUnknownObservationType() {
        final MooseDataCardLargeCarnivoreObservation input =
                newObservationWithinSeason().withObservationType("invalid");
        assertAbandonReason(input, observationTypeOfLargeCarnivoreContainsIllegalCharacters(input));
    }

    @Test
    public void testWhenNoAmountIsGreaterThanZero() {
        Stream.of(0, -1, null).forEach(amount -> {
            final MooseDataCardLargeCarnivoreObservation input = newObservationWithinSeason()
                    .withNumberOfWolves(amount)
                    .withNumberOfBears(amount)
                    .withNumberOfLynxes(amount)
                    .withNumberOfWolverines(amount);

            assertAbandonReason(input, sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero(input));
        });
    }
}
