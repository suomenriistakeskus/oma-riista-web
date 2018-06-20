package fi.riista.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import static java.util.Objects.requireNonNull;

public final class NumberUtils {

    public static final double EPSILON = 0.00001;

    public static final BinaryOperator<Integer> NULLSAFE_INT_SUM = (a, b) -> nullsafeSum(a, b);

    private NumberUtils() {
        throw new AssertionError();
    }

    public static int getIntValueOrZero(@Nullable final Number n) {
        return F.coalesceAsInt(n, 0);
    }

    public static boolean equal(final Float num1, final Float num2) {
        return num1 == null ? num2 == null : num2 != null && Math.abs(num1 - num2) < EPSILON;
    }

    public static boolean equal(final Double num1, final Double num2) {
        return num1 == null ? num2 == null : num2 != null && Math.abs(num1 - num2) < EPSILON;
    }

    public static void bigDecimalEquals(long expected, BigDecimal actual) {
        NumberUtils.bigDecimalEquals(BigDecimal.valueOf(expected), actual);
    }

    public static void bigDecimalEquals(int expected, BigDecimal actual) {
        NumberUtils.bigDecimalEquals(BigDecimal.valueOf(expected), actual);
    }

    public static void bigDecimalEquals(BigDecimal expected, long actual) {
        NumberUtils.bigDecimalEquals(expected, BigDecimal.valueOf(actual));
    }

    public static void bigDecimalEquals(BigDecimal expected, int actual) {
        NumberUtils.bigDecimalEquals(expected, BigDecimal.valueOf(actual));
    }

    public static void bigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        final boolean eitherNull = expected == null && actual != null || expected != null && actual == null;

        if (eitherNull || expected != null && actual != null && expected.compareTo(actual) != 0) {
            throw new AssertionError(String.format("expected %s but was: %s", expected, actual));
        }
    }

    public static boolean isInRange(final double value, final double min, final double max) {
        return value > min && value < max || equal(value, min) || equal(value, max);
    }

    public static long squareMetersToHectares(double squareMeters) {
        return Math.round(squareMeters / 10_000);
    }

    public static <T> int sum(@Nonnull final Collection<? extends T> collection,
                              @Nonnull final ToIntFunction<? super T> mapper) {

        requireNonNull(collection, "collection is null");
        requireNonNull(mapper, "mapper is null");

        return collection.stream().mapToInt(mapper).sum();
    }

    public static <T> double sum(@Nonnull final Collection<? extends T> collection,
                                 @Nonnull final ToDoubleFunction<? super T> mapper) {

        requireNonNull(collection, "collection is null");
        requireNonNull(mapper, "mapper is null");

        return collection.stream().mapToDouble(mapper).sum();
    }

    @Nonnull
    public static <T> BigDecimal sum(@Nonnull final Collection<? extends T> collection,
                                     @Nonnull final Function<? super T, BigDecimal> mapper) {

        requireNonNull(collection, "collection is null");
        requireNonNull(mapper, "mapper is null");

        return collection.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static <T, N extends Number> int sumNullsToZero(@Nonnull final Iterable<? extends T> iterable,
                                                           @Nonnull final Function<? super T, N> mapper) {

        requireNonNull(iterable, "iterable is null");
        requireNonNull(mapper, "mapper is null");

        return F.stream(iterable).map(mapper).mapToInt(NumberUtils::getIntValueOrZero).sum();
    }

    @Nullable
    public static Integer nullsafeSum(@Nullable final Integer a, @Nullable final Integer b) {
        if (a == null) {
            return b;
        }
        return a.intValue() + getIntValueOrZero(b);
    }

    @Nullable
    @SafeVarargs
    public static <T> Integer nullsafeSum(@Nonnull final Function<? super T, Integer> mapper,
                                          @Nonnull final T... objects) {

        requireNonNull(mapper, "mapper is null");
        requireNonNull(objects, "objects is null");

        return Arrays.stream(objects).map(mapper).reduce(null, NULLSAFE_INT_SUM);
    }

    @Nullable
    public static <T, N extends Number> Integer nullsafeSumAsInt(@Nullable final T first,
                                                                 @Nullable final T second,
                                                                 @Nonnull final Function<? super T, N> mapper) {

        requireNonNull(mapper, "mapper is null");

        final Optional<Integer> secondValueOpt = Optional.ofNullable(second).map(mapper).map(Number::intValue);

        return Optional.ofNullable(first)
                .map(mapper)
                .map(Number::intValue)
                .map(firstValue -> firstValue + secondValueOpt.orElse(0))
                .orElseGet(() -> secondValueOpt.orElse(null));
    }

    @Nullable
    public static <T> BigDecimal nullsafeSum(@Nullable final T first,
                                             @Nullable final T second,
                                             @Nonnull final Function<? super T, BigDecimal> mapper) {

        requireNonNull(mapper, "mapper is null");

        final Optional<BigDecimal> secondValueOpt = Optional.ofNullable(second).map(mapper);

        return Optional.ofNullable(first)
                .map(mapper)
                .map(firstValue -> firstValue.add(secondValueOpt.orElse(BigDecimal.ZERO)))
                .orElseGet(() -> secondValueOpt.orElse(null));
    }
}
