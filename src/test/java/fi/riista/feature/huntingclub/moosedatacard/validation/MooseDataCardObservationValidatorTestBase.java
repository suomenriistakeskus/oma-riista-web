package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationAbandonedBecauseOfMissingDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;

import javaslang.control.Either;

import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.function.Consumer;

public abstract class MooseDataCardObservationValidatorTestBase<T extends DateAndLocation> {

    protected static final GeoLocation DEFAULT_COORDINATES = new GeoLocation(3, 7);

    protected abstract MooseDataCardObservationValidator<T> getValidator(@Nonnull GeoLocation defaultCoordinates);

    protected abstract T newObservation();

    @Test
    public void testMissingDate() {
        final T input = newObservation();
        input.setDate(null);
        assertAbandonReason(input, observationAbandonedBecauseOfMissingDate(input));
    }

    @Test
    public void testMissingLatitude() {
        final T input = newObservation();
        input.setLatitude(null);
        assertAccepted(input, output -> assertEquals(DEFAULT_COORDINATES, output.getGeoLocation()));
    }

    @Test
    public void testMissingLongitude() {
        final T input = newObservation();
        input.setLongitude(null);
        assertAccepted(input, output -> assertEquals(DEFAULT_COORDINATES, output.getGeoLocation()));
    }

    protected void assertAccepted(final T input, final Consumer<T> consumer) {
        final Either<String, T> either = getValidator(DEFAULT_COORDINATES).validate(input);
        assertTrue(either.isRight());
        consumer.accept(either.get());
    }

    protected void assertAbandonReason(final T input, final String expectedMsg) {
        final Either<String, T> either = getValidator(DEFAULT_COORDINATES).validate(input);
        assertTrue(either.isLeft());
        assertEquals(expectedMsg, either.getLeft());
    }

}
