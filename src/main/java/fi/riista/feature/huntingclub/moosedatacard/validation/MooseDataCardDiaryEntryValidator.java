package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import io.vavr.control.Validation;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.diaryEntryMissingDate;
import static fi.riista.util.DateUtil.copyDateForHuntingYear;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

public abstract class MooseDataCardDiaryEntryValidator<T extends DateAndLocation> {

    protected final int huntingYear;
    protected final GeoLocation defaultCoordinates;

    public MooseDataCardDiaryEntryValidator(final int huntingYear, @Nonnull final GeoLocation defaultCoordinates) {
        Preconditions.checkArgument(huntingYear >= 2015, "Illegal hunting year: " + huntingYear);

        this.huntingYear = huntingYear;
        this.defaultCoordinates = Objects.requireNonNull(defaultCoordinates);
    }

    protected Validation<String, LocalDate> validateDate(@Nonnull final T entry) {
        Objects.requireNonNull(entry);

        return entry.getDate() == null
                ? invalid(diaryEntryMissingDate(entry))
                : valid(copyDateForHuntingYear(entry.getDate(), huntingYear));
    }

    protected GeoLocation getValidGeoLocation(@Nonnull final T entry) {
        return MooseDataCardDiaryEntryField.LATITUDE.findValid(entry)
                .flatMap(latitude -> MooseDataCardDiaryEntryField.LONGITUDE.findValid(entry)
                        .map(longitude -> new GeoLocation(latitude.intValue(), longitude.intValue())))
                .orElse(defaultCoordinates);
    }
}
