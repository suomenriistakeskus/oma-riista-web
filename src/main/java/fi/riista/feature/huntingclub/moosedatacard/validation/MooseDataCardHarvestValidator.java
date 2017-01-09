package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.harvestDateNotWithinPermittedSeason;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

import com.kscs.util.jaxb.Copyable;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;

import javaslang.control.Validation;

import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

public abstract class MooseDataCardHarvestValidator<T extends MooseDataCardHarvest & Copyable>
        extends MooseDataCardDiaryEntryValidator<T> {

    protected final Has2BeginEndDates permitSeason;

    public MooseDataCardHarvestValidator(@Nonnull final GeoLocation defaultCoordinates) {
        super(defaultCoordinates);
        this.permitSeason = null;
    }

    public MooseDataCardHarvestValidator(
            @Nonnull final Has2BeginEndDates permitSeason, @Nonnull final GeoLocation defaultCoordinates) {

        super(defaultCoordinates);
        this.permitSeason = Objects.requireNonNull(permitSeason, "permitSeason is null");
    }

    public abstract Validation<List<String>, T> validate(@Nonnull final T harvest);

    protected Validation<String, T> getCommonHarvestValidation(@Nonnull final T input) {
        Objects.requireNonNull(input);

        return validateDate(input).flatMap(date -> {
            final T output = (T) input.createCopy();
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
