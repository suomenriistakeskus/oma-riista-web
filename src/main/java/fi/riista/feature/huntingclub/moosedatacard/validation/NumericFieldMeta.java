package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.google.common.collect.Range;
import fi.riista.util.ValidationUtils;
import io.vavr.control.Validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

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
        return findFieldValue(obj).orElse(null);
    }

    @Nonnull
    public Validation<String, Optional<N>> validate(@Nullable final T obj) {
        return mapToValidation(findFieldValue(obj), num -> range.contains(num), getInvalidMessageFunction(obj));
    }

    @Nonnull
    public Validation<String, Optional<N>> validateRangeAndEquality(
            @Nullable final T obj,
            @Nonnull final N expected,
            @Nonnull final Function<N, String> onNotMatchingMessageFunction) {

        Objects.requireNonNull(expected, "expected is null");
        Objects.requireNonNull(onNotMatchingMessageFunction, "onNotMatchingMessageFunction is null");

        return validate(obj).flatMap(numOpt -> mapToValidation(numOpt, expected::equals, onNotMatchingMessageFunction));
    }

    private Optional<N> findFieldValue(@Nullable final T obj) {
        return Optional.ofNullable(obj).map(this::doApply);
    }

    private Validation<String, Optional<N>> mapToValidation(final Optional<N> optional,
                                                            final Predicate<N> predicate,
                                                            final Function<N, String> onFailingTestMessageFunction) {
        return ValidationUtils.toValidation(
                optional,
                num -> predicate.test(num) ? valid(optional) : invalid(onFailingTestMessageFunction.apply(num)),
                () -> valid(Optional.empty()));
    }

    @Nonnull
    public Optional<N> findValid(@Nullable final T obj) {
        return validate(obj).fold(invalid -> Optional.empty(), Function.identity());
    }

    @Nullable
    public N getValidOrNull(@Nullable final T obj) {
        return validate(obj).fold(invalid -> null, opt -> opt.orElse(null));
    }

    public boolean isValueInRange(@Nullable final N value) {
        return value != null && range.contains(value);
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
