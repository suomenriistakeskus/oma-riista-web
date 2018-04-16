package fi.riista.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javaslang.control.Validation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

public final class ValidationUtils {

    @Nonnull
    public static <E, T> Validation<E, T> toValidation(@Nonnull final Optional<T> optional,
                                                       @Nonnull final Supplier<Validation<E, T>> onEmptyResultSupplier) {

        return toValidation(optional, Validation::valid, onEmptyResultSupplier);
    }

    @Nonnull
    public static <E, T, U> Validation<E, U> toValidation(@Nonnull final Optional<T> optional,
                                                          @Nonnull final Function<? super T, Validation<E, U>> mapper,
                                                          @Nonnull final Supplier<Validation<E, U>> onEmptyResultSupplier) {
        Objects.requireNonNull(optional, "optional is null");
        Objects.requireNonNull(mapper, "mapper is null");
        Objects.requireNonNull(onEmptyResultSupplier, "onEmptyResultSupplier is null");

        return optional.map(mapper).orElseGet(onEmptyResultSupplier);
    }

    @Nonnull
    public static <E, T> Validation<List<E>, List<T>> combine(
            @Nonnull final Stream<? extends Validation<? extends Iterable<E>, T>> stream) {

        Objects.requireNonNull(stream);

        final ArrayList<E> errors = new ArrayList<>();
        final ArrayList<T> values = new ArrayList<>();

        stream.forEach(validation -> {
            if (validation.isInvalid()) {
                Iterables.addAll(errors, validation.getError());
            } else if (errors.isEmpty()) {
                values.add(validation.get());
            }
        });

        return errors.isEmpty() ? valid(values) : invalid(errors);
    }

    @Nonnull
    public static <E, T, U, R> Validation<List<E>, R> reduce(
          @Nonnull final Validation<? extends Iterable<E>, T> first,
          @Nonnull final Validation<? extends Iterable<E>, U> second,
          @Nonnull final BiFunction<T, U, R> mapper) {

        Objects.requireNonNull(first, "first is null");
        Objects.requireNonNull(second, "second is null");
        Objects.requireNonNull(mapper, "mapper is null");

        if (first.isValid()) {
            return second.isValid()
                    ? valid(mapper.apply(first.get(), second.get()))
                    : invalid(Lists.newArrayList(second.getError()));
        }

        final ArrayList<E> errors = Lists.newArrayList(first.getError());

        if (second.isValid()) {
            return invalid(errors);
        }

        Iterables.addAll(errors, second.getError());
        return invalid(errors);
    }

    @Nonnull
    public static <E, T> Validation<List<E>, T> validate(
            @Nonnull final T obj,
            @Nonnull final Iterable<? extends Function<? super T, ? extends Validation<E, ?>>> validationFunctions) {

        Objects.requireNonNull(obj, "obj is null");
        Objects.requireNonNull(validationFunctions, "validationFunctions is null");

        final List<E> errors = F.stream(validationFunctions)
                .map(fn -> fn.apply(obj))
                .filter(Validation::isInvalid)
                .map(Validation::getError)
                .collect(toList());

        return errors.isEmpty() ? valid(obj) : invalid(errors);
    }

    private ValidationUtils() {
        throw new AssertionError();
    }

}
