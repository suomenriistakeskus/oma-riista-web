package fi.riista.feature.gamediary;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum GameDiaryEntryType {

    HARVEST,
    OBSERVATION,
    SRVA;

    public <T> T selectValue(@Nullable final T valueForHarvest, @Nullable final T valueForObservation) {
        switch (this) {
            case HARVEST:
                return valueForHarvest;
            case OBSERVATION:
                return valueForObservation;
            default:
                throw new IllegalArgumentException("Unsupported game diary entry type: " + this.name());
        }
    }

    public <T extends GameDiaryEntry> void consume(
            @Nonnull final T diaryEntry,
            @Nonnull final Consumer<Harvest> harvestConsumer,
            @Nonnull final Consumer<Observation> observationConsumer) {

        Objects.requireNonNull(diaryEntry, "diaryEntry is null");
        Objects.requireNonNull(harvestConsumer, "harvestConsumer is null");
        Objects.requireNonNull(observationConsumer, "observationConsumer is null");

        switch (this) {
            case HARVEST:
                harvestConsumer.accept(Harvest.class.cast(diaryEntry));
                break;
            case OBSERVATION:
                observationConsumer.accept(Observation.class.cast(diaryEntry));
                break;
            default:
                throw new IllegalArgumentException("Unsupported game diary entry type: " + this.name());
        }
    }

    @Nullable
    public <T> T supply(
            @Nonnull final Supplier<T> onHarvestSupplier, @Nonnull final Supplier<T> onObservationSupplier) {

        Objects.requireNonNull(onHarvestSupplier, "onHarvestSupplier is null");
        Objects.requireNonNull(onObservationSupplier, "onObservationSupplier is null");

        switch (this) {
            case HARVEST:
                return onHarvestSupplier.get();
            case OBSERVATION:
                return onObservationSupplier.get();
            default:
                throw new IllegalArgumentException("Unsupported game diary entry type: " + this.name());
        }
    }

    @Nullable
    public <T extends GameDiaryEntry, U> U apply(
            @Nonnull final T diaryEntry,
            @Nonnull final Function<Harvest, U> harvestFunction,
            @Nonnull final Function<Observation, U> observationFunction) {

        Objects.requireNonNull(diaryEntry, "diaryEntry is null");
        Objects.requireNonNull(harvestFunction, "harvestFunction is null");
        Objects.requireNonNull(observationFunction, "observationFunction is null");

        switch (this) {
            case HARVEST:
                return harvestFunction.apply(Harvest.class.cast(diaryEntry));
            case OBSERVATION:
                return observationFunction.apply(Observation.class.cast(diaryEntry));
            default:
                throw new IllegalArgumentException("Unsupported game diary entry type: " + this.name());
        }
    }

}
