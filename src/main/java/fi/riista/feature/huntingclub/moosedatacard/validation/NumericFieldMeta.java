package fi.riista.feature.huntingclub.moosedatacard.validation;

import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

import com.google.common.collect.Range;

import javaslang.control.Validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class NumericFieldMeta<T, N extends Number & Comparable<N>> implements Function<T, N> {

    private final String nameFinnish;
    private final Range<N> range;

    protected NumericFieldMeta(@Nonnull final String nameFinnish, @Nonnull final Range<N> range) {
        this.nameFinnish = Objects.requireNonNull(nameFinnish, "nameFinnish is null");
        this.range = Objects.requireNonNull(range, "range is null");
    }

    @Nullable
    protected abstract N doApply(@Nonnull T obj);

    @Nonnull
    protected abstract Function<N, String> getInvalidMessageFunction(@Nonnull T obj);

    @Nullable
    @Override
    public N apply(@Nullable final T obj) {
        return Optional.ofNullable(obj).map(this::doApply).orElse(null);
    }

    @Nonnull
    public Validation<String, Optional<N>> validate(@Nullable final T obj) {
        return Optional.ofNullable(obj)
                .map(this::doApply)
                .<Validation<String, Optional<N>>> map(value -> range.contains(value)
                        ? valid(Optional.of(value))
                        : invalid(getInvalidMessageFunction(obj).apply(value)))
                .orElseGet(() -> valid(Optional.empty()));
    }

    @Nonnull
    public Optional<N> getValidOrEmpty(@Nullable final T obj) {
        return validate(obj).fold(invalid -> Optional.empty(), Function.identity());
    }

    @Nullable
    public N getValidOrNull(@Nullable final T obj) {
        return validate(obj).fold(invalid -> null, opt -> opt.orElse(null));
    }

    public boolean isValueInRange(@Nullable final N value) {
        return value != null && range.contains(value);
    }

    public void ifNotNullAndValid(@Nullable final N value, @Nonnull final Consumer<N> consumer) {
        if (isValueInRange(value)) {
            consumer.accept(value);
        }
    }

    public Optional<N> findLowerBound() {
        return Optional.ofNullable(range.lowerEndpoint());
    }

    public Optional<N> findUpperBound() {
        return Optional.ofNullable(range.upperEndpoint());
    }

    // Accessors -->

    public String getNameFinnish() {
        return nameFinnish;
    }

    public Range<N> getRange() {
        return range;
    }

}
