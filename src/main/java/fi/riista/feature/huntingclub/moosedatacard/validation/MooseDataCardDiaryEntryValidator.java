package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.diaryEntryMissingDate;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;

import javaslang.control.Validation;

import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import java.util.Objects;

public abstract class MooseDataCardDiaryEntryValidator<T extends DateAndLocation> {

    protected final GeoLocation defaultCoordinates;

    public MooseDataCardDiaryEntryValidator(@Nonnull final GeoLocation defaultCoordinates) {
        this.defaultCoordinates = Objects.requireNonNull(defaultCoordinates);
    }

    protected Validation<String, LocalDate> validateDate(@Nonnull final T entry) {
        return entry.getDate() == null ? invalid(diaryEntryMissingDate(entry)) : valid(entry.getDate());
    }

    protected GeoLocation getValidGeoLocation(@Nonnull final T entry) {
        return MooseDataCardDiaryEntryField.LATITUDE.findValid(entry)
                .flatMap(latitude -> MooseDataCardDiaryEntryField.LONGITUDE.findValid(entry)
                        .map(longitude -> new GeoLocation(latitude.intValue(), longitude.intValue())))
                .orElse(defaultCoordinates);
    }

}
