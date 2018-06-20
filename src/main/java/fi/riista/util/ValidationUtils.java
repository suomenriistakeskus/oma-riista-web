package fi.riista.util;

import com.google.common.collect.Iterables;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static java.util.stream.Collectors.collectingAndThen;

public final class ValidationUtils {

    @Nonnull
    public static <E, T> Validation<List<E>, T> toValidation(@Nonnull final List<E> errors, final T value) {
        return errors.isEmpty() ? valid(value) : invalid(errors);
    }

    @Nonnull
    public static <E, T> Validation<List<E>, T> toValidation(@Nonnull final List<E> errors,
                                                             @Nonnull final Supplier<T> valueSupplier) {

        return errors.isEmpty() ? valid(valueSupplier.get()) : invalid(errors);
    }

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
    public static <E> Collector<Validation<? extends Iterable<E>, ?>, List<E>, List<E>> errorFlattener() {
        return Collect.toListBy((errors, validation) -> {
            if (validation.isInvalid()) {
                Iterables.addAll(errors, validation.getError());
            }
        });
    }

    @Nonnull
    public static <E, R> Collector<Validation<? extends Iterable<E>, ?>, List<E>, Validation<List<E>, R>> flattenErrorsOrElse(
            @Nullable final R result) {

        return collectingAndThen(errorFlattener(), errors -> toValidation(errors, result));
    }

    @Nonnull
    public static <E, R> Collector<Validation<? extends Iterable<E>, ?>, List<E>, Validation<List<E>, R>> flattenErrorsOrElseGet(
            @Nonnull final Supplier<R> resultSupplier) {

        Objects.requireNonNull(resultSupplier);
        return collectingAndThen(errorFlattener(), validationFn(resultSupplier));
    }

    @Nonnull
    public static <E, T> Collector<Validation<? extends Iterable<E>, T>, ?, Validation<List<E>, List<T>>> combining() {

        final BiConsumer<Tuple2<List<E>, List<T>>, Validation<? extends Iterable<E>, T>> accumulator =
                (tuple, validation) -> {
                    final List<E> accumulatedErrors = tuple._1;

                    if (validation.isInvalid()) {
                        Iterables.addAll(accumulatedErrors, validation.getError());
                    } else if (accumulatedErrors.isEmpty()) {
                        tuple._2.add(validation.get());
                    }
                };

        final BinaryOperator<Tuple2<List<E>, List<T>>> combiner = (left, right) -> {
            left._1.addAll(right._1);
            left._2.addAll(right._2);
            return left;
        };

        return collectingAndThen(
                Collector.of(() -> Tuple.of(new ArrayList<E>(), new ArrayList<T>()), accumulator, combiner),
                listPair -> toValidation(listPair._1, listPair._2));
    }

    @Nonnull
    public static <E, T> Collector<Function<? super T, ? extends Validation<E, ?>>, List<E>, Validation<List<E>, T>> applying(
            @Nullable final T obj) {

        final BiConsumer<List<E>, Function<? super T, ? extends Validation<E, ?>>> accumulator = (errors, function) -> {
            final Validation<E, ?> validation = function.apply(obj);

            if (validation.isInvalid()) {
                errors.add(validation.getError());
            }
        };

        return collectingAndThen(Collect.toListBy(accumulator), errors -> toValidation(errors, obj));
    }

    private static <E, T> Function<List<E>, Validation<List<E>, T>> validationFn(final Supplier<T> validSupplier) {
        return errors -> toValidation(errors, validSupplier);
    }

    private ValidationUtils() {
        throw new AssertionError();
    }
}
