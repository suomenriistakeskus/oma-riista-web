package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import io.vavr.control.Either;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationAbandonedBecauseOfMissingDate;
import static fi.riista.test.TestUtils.ld;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class MooseDataCardObservationValidatorTestBase<T extends DateAndLocation> {

    protected static final GeoLocation DEFAULT_COORDINATES = new GeoLocation(3, 7);

    protected Has2BeginEndDates permitSeason;

    @Before
    public void setup() {
        permitSeason = new Has2BeginEndDatesDTO(ld(2015, 9, 1), ld(2015, 12, 31));
    }

    protected abstract MooseDataCardObservationValidator<T> getValidator(int huntingYear,
                                                                         @Nonnull GeoLocation defaultCoordinates);

    protected abstract T newObservation(@Nullable final LocalDate date);

    protected T newObservationWithinSeason() {
        return newObservation(permitSeason.getFirstDate());
    }

    @Test
    public void testMissingDate() {
        final T input = newObservation(null);

        assertAbandonReason(input, observationAbandonedBecauseOfMissingDate(input));
    }

    @Test
    public void testCorrectionOfWrongHuntingYear() {
        final LocalDate firstPermitDate = permitSeason.getFirstDate();
        final T input = newObservation(firstPermitDate.plusYears(2));

        assertAccepted(input, output -> assertEquals(firstPermitDate, output.getDate()));
    }

    @Test
    public void testMissingLatitude() {
        final T input = newObservationWithinSeason();
        input.setLatitude(null);

        assertAccepted(input, output -> assertEquals(DEFAULT_COORDINATES, output.getGeoLocation()));
    }

    @Test
    public void testMissingLongitude() {
        final T input = newObservationWithinSeason();
        input.setLongitude(null);

        assertAccepted(input, output -> assertEquals(DEFAULT_COORDINATES, output.getGeoLocation()));
    }

    protected void assertAccepted(final T input, final Consumer<T> consumer) {
        final Either<String, T> either = validate(input);
        assertTrue(either.isRight());
        consumer.accept(either.get());
    }

    protected void assertAbandonReason(final T input, final String expectedMsg) {
        final Either<String, T> either = validate(input);
        assertTrue(either.isLeft());
        assertEquals(expectedMsg, either.getLeft());
    }

    protected Either<String, T> validate(final T input) {
        return getValidator(permitSeason.resolveHuntingYear(), DEFAULT_COORDINATES).validate(input);
    }
}
