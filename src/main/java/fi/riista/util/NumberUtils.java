package fi.riista.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public final class NumberUtils {

    public static final BigDecimal MAX_PERCENTAGE_SHARE = new BigDecimal(100);

    public static final double EPSILON = 0.00001;

    public static int getIntValueOrZero(@Nullable final Number n) {
        return F.coalesceAsInt(n, 0);
    }

    public static boolean isPositive(@Nonnull final BigDecimal number) {
        return number.compareTo(ZERO) > 0;
    }

    public static boolean isNegative(@Nonnull final BigDecimal number) {
        return number.compareTo(ZERO) < 0;
    }

    public static boolean equal(final Float num1, final Float num2) {
        return num1 == null ? num2 == null : num2 != null && Math.abs(num1 - num2) < EPSILON;
    }

    public static boolean equal(final Double num1, final Double num2) {
        return num1 == null ? num2 == null : num2 != null && Math.abs(num1 - num2) < EPSILON;
    }

    public static boolean bigDecimalIsPositive(final BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static void bigDecimalEquals(final long expected, final BigDecimal actual) {
        NumberUtils.bigDecimalEquals(BigDecimal.valueOf(expected), actual);
    }

    public static void bigDecimalEquals(final int expected, final BigDecimal actual) {
        NumberUtils.bigDecimalEquals(BigDecimal.valueOf(expected), actual);
    }

    public static void bigDecimalEquals(final BigDecimal expected, final long actual) {
        NumberUtils.bigDecimalEquals(expected, BigDecimal.valueOf(actual));
    }

    public static void bigDecimalEquals(final BigDecimal expected, final int actual) {
        NumberUtils.bigDecimalEquals(expected, BigDecimal.valueOf(actual));
    }

    public static void bigDecimalEquals(final BigDecimal expected, final BigDecimal actual) {
        final boolean eitherNull = expected == null && actual != null || expected != null && actual == null;

        if (eitherNull || expected != null && actual != null && expected.compareTo(actual) != 0) {
            throw new AssertionError(String.format("expected %s but was: %s", expected, actual));
        }
    }

    public static boolean isInRange(final double value, final double min, final double max) {
        return value > min && value < max || equal(value, min) || equal(value, max);
    }

    public static long squareMetersToHectares(final double squareMeters) {
        return Math.round(squareMeters / 10_000);
    }

    public static BigDecimal squareMetersToHectares(final double squareMeters, final int precision) {
        checkArgument(precision > 0);

        return BigDecimal.valueOf(squareMeters / 10_000)
                .setScale(precision, RoundingMode.HALF_UP);
    }

    public static double percentRatio(final double a, final double b) {
        return Math.round(b) == 0 ? 0 : 100.0 * a / b;
    }

    public static double ratio(final double a, final double b) {
        return Math.round(b) == 0 ? 0 : a / b;
    }

    public static <T> int sum(@Nonnull final Iterable<? extends T> iterable,
                              @Nonnull final ToIntFunction<? super T> mapper) {

        requireNonNull(iterable, "iterable is null");
        requireNonNull(mapper, "mapper is null");

        return F.stream(iterable).mapToInt(mapper).sum();
    }

    public static <T> double sum(@Nonnull final Iterable<? extends T> iterable,
                                 @Nonnull final ToDoubleFunction<? super T> mapper) {

        requireNonNull(iterable, "iterable is null");
        requireNonNull(mapper, "mapper is null");

        return F.stream(iterable).mapToDouble(mapper).sum();
    }

    @Nonnull
    public static <T> BigDecimal sum(@Nonnull final Iterable<? extends T> iterable,
                                     @Nonnull final Function<? super T, BigDecimal> mapper) {

        requireNonNull(iterable, "iterable is null");
        requireNonNull(mapper, "mapper is null");

        return F.stream(iterable).map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Nonnull
    public static <T> BigDecimal currencySum(@Nonnull final Iterable<? extends T> iterable,
                                             @Nonnull final Function<? super T, BigDecimal> mapper) {

        return sum(iterable, mapper).setScale(2);
    }

    @Nullable
    public static Integer nullableIntSum(@Nullable final Integer first, @Nullable final Integer second) {
        if (first == null) {
            return second;
        }
        return first.intValue() + getIntValueOrZero(second);
    }

    @Nullable
    public static <T> Integer nullableIntSum(@Nullable final T first,
                                             @Nullable final T second,
                                             @Nonnull final Function<? super T, Integer> mapper) {

        requireNonNull(mapper, "mapper is null");

        final Integer firstInteger = first != null ? mapper.apply(first) : null;
        final Integer secondInteger = second != null ? mapper.apply(second) : null;

        return nullableIntSum(firstInteger, secondInteger);
    }

    @Nullable
    public static Integer nullableIntSum(@Nonnull final Stream<Integer> integers) {
        requireNonNull(integers);
        return integers.reduce(null, NumberUtils::nullableIntSum);
    }

    @Nullable
    @SafeVarargs
    public static Integer nullableIntSum(@Nonnull final Integer... integers) {
        requireNonNull(integers);
        return nullableIntSum(stream(integers));
    }

    @Nullable
    public static <T> Integer nullableIntSum(@Nonnull final Iterable<? extends T> iterable,
                                             @Nonnull final Function<? super T, Integer> mapper) {

        return nullableIntSum(F.stream(iterable).map(mapper));
    }

    @Nullable
    public static Double nullableDoubleSubtraction(@Nullable final Double first, @Nullable final Double second) {
        if (first == null || second == null) {
            return null;
        }
        return first - second;
    }

    public static <T> int nullsafeIntSum(@Nonnull final Iterable<? extends T> iterable,
                                         @Nonnull final Function<? super T, Integer> mapper) {

        return F.stream(iterable)
                .map(mapper)
                .mapToInt(i -> F.coalesceAsInt(i, 0))
                .sum();
    }

    @Nullable
    public static BigDecimal nullableSum(@Nullable final BigDecimal first, @Nullable final BigDecimal second) {
        if (first == null) {
            return second;
        }
        return second == null ? first : first.add(second);
    }

    @Nullable
    public static <T> BigDecimal nullableSum(@Nullable final T first,
                                             @Nullable final T second,
                                             @Nonnull final Function<? super T, BigDecimal> mapper) {

        requireNonNull(mapper, "mapper is null");

        final BigDecimal firstNumber = first != null ? mapper.apply(first) : null;
        final BigDecimal secondNumber = second != null ? mapper.apply(second) : null;

        return nullableSum(firstNumber, secondNumber);
    }

    @Nullable
    public static BigDecimal nullableSum(@Nonnull final Stream<BigDecimal> numbers) {
        requireNonNull(numbers);
        return numbers.reduce(null, NumberUtils::nullableSum);
    }

    @Nullable
    @SafeVarargs
    public static BigDecimal nullableSum(@Nonnull final BigDecimal... numbers) {
        requireNonNull(numbers);
        return nullableSum(stream(numbers));
    }

    @Nullable
    public static <T> BigDecimal nullableSum(@Nonnull final Iterable<? extends T> iterable,
                                             @Nonnull final Function<? super T, BigDecimal> mapper) {

        requireNonNull(iterable, "iterable is null");
        requireNonNull(mapper, "mapper is null");

        return F.stream(iterable)
                .map(mapper)
                .filter(Objects::nonNull)
                .reduce(null, NumberUtils::nullableSum);
    }

    @Nonnull
    public static BigDecimal currencySum(@Nonnull final Iterable<BigDecimal> numbers) {
        return F.stream(numbers).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
    }

    private NumberUtils() {
        throw new AssertionError();
    }
}
