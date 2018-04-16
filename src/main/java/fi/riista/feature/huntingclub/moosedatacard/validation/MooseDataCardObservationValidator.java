package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationAbandonedBecauseOfMissingDate;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import javaslang.control.Either;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class MooseDataCardObservationValidator<T extends DateAndLocation>
        extends MooseDataCardDiaryEntryValidator<T> {

    public MooseDataCardObservationValidator(@Nonnull final GeoLocation defaultCoordinates) {
        super(defaultCoordinates);
    }

    /**
     * Returns either a textual reason for discarding observation or a valid observation.
     */
    public abstract Either<String, T> validate(@Nonnull T object);

    protected Either<String, LocalDate> resolveDate(@Nonnull final T object) {
        return validateDate(Objects.requireNonNull(object))
                .toEither()
                .mapLeft(invalidMsg -> observationAbandonedBecauseOfMissingDate(object));
    }

}
