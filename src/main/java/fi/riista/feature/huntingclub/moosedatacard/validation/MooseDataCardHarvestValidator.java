package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.kscs.util.jaxb.Copyable;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import io.vavr.control.Validation;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.harvestDateNotWithinPermittedSeason;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

public abstract class MooseDataCardHarvestValidator<T extends MooseDataCardHarvest & Copyable>
        extends MooseDataCardDiaryEntryValidator<T> {

    protected final Has2BeginEndDates permitSeason;

    public MooseDataCardHarvestValidator(@Nonnull final Has2BeginEndDates permitSeason,
                                         @Nonnull final GeoLocation defaultCoordinates) {

        super(Objects.requireNonNull(permitSeason, "permitSeason is null").resolveHuntingYear(), defaultCoordinates);

        this.permitSeason = permitSeason;
    }

    public abstract Validation<List<String>, T> validate(@Nonnull final T harvest);

    protected Validation<String, T> getCommonHarvestValidation(@Nonnull final T input) {
        return validateDate(input).flatMap(date -> {
            final T output = (T) input.createCopy();
            output.setDate(date);
            output.setGeoLocation(getValidGeoLocation(input));
            output.setWeightEstimated(MooseDataCardDiaryEntryField.ESTIMATED_WEIGHT.getValidOrNull(input));
            output.setWeightMeasured(MooseDataCardDiaryEntryField.MEASURED_WEIGHT.getValidOrNull(input));
            output.setFitnessClass(input.findFitnessClassAsInteger().map(Object::toString).orElse(null));
            return valid(output);
        });
    }

    @Override
    protected Validation<String, LocalDate> validateDate(@Nonnull final T harvest) {
        return super.validateDate(harvest).flatMap(date -> permitSeason == null || permitSeason.containsDate(date)
                ? valid(date)
                : invalid(harvestDateNotWithinPermittedSeason(harvest, permitSeason)));
    }
}
