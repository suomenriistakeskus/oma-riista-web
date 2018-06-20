package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.util.F;
import io.vavr.control.Try;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;

public interface MooseDataCardHarvest extends DateAndLocation {

    Double getWeightMeasured();

    void setWeightMeasured(Double weight);

    Double getWeightEstimated();

    void setWeightEstimated(Double weight);

    String getFitnessClass();

    void setFitnessClass(String fitnessClass);

    String getAdditionalInfo();

    void setAdditionalInfo(String text);

    boolean isNotEdible();

    void setNotEdible(boolean edible);

    @Override
    default boolean isEmpty() {
        return DateAndLocation.super.isEmpty() && F.allNull(
                getWeightMeasured(), getWeightEstimated(), emptyToNull(getFitnessClass()),
                emptyToNull(getAdditionalInfo()));
    }

    default boolean isWeightPresent() {
        return getWeightMeasured() != null || getWeightEstimated() != null;
    }

    @Nonnull
    default Optional<Double> findWeight() {
        return Optional.ofNullable(Optional.ofNullable(getWeightMeasured()).orElseGet(this::getWeightEstimated));
    }

    @Nonnull
    default Try<Integer> parseFitnessClass() {
        return MooseDataCardExtractor.parseNumber(getFitnessClass(), Integer::parseInt);
    }

    @Nonnull
    default Optional<Integer> findFitnessClassAsInteger() {
        return parseFitnessClass()
                .map(Optional::ofNullable)
                .getOrElseGet(throwable -> Optional.empty());
    }

    @Nullable
    default Integer getFitnessClassAsInteger() {
        return findFitnessClassAsInteger().orElse(null);
    }
}
