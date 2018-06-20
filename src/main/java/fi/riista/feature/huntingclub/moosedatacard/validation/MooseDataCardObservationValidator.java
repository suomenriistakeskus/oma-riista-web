package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import io.vavr.control.Either;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationAbandonedBecauseOfMissingDate;

public abstract class MooseDataCardObservationValidator<T extends DateAndLocation>
        extends MooseDataCardDiaryEntryValidator<T> {

    public MooseDataCardObservationValidator(final int huntingYear, @Nonnull final GeoLocation defaultCoordinates) {
        super(huntingYear, defaultCoordinates);
    }

    /**
     * Returns either a textual reason for discarding observation or a valid observation.
     */
    public abstract Either<String, T> validate(@Nonnull T object);

    protected Either<String, LocalDate> resolveDate(@Nonnull final T object) {
        return validateDate(object)
                .toEither()
                .mapLeft(invalidMsg -> observationAbandonedBecauseOfMissingDate(object));
    }
}
