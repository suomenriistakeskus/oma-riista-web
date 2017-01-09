package fi.riista.util;

import static org.junit.Assert.fail;

import com.google.common.base.Preconditions;

import fi.riista.config.Constants;
import fi.riista.feature.error.RevisionConflictException;

import javaslang.Tuple;
import javaslang.Tuple2;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import javax.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TestUtils {

    public static <T> T unwrapProxy(final Class<T> clazz, final Object proxy) {
        Object target = proxy;

        if (AopUtils.isAopProxy(proxy) && proxy instanceof Advised) {
            try {
                target = Advised.class.cast(proxy).getTargetSource().getTarget();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (target != null && clazz.isAssignableFrom(target.getClass())) {
            return clazz.cast(target);
        }

        throw new IllegalArgumentException("proxy object does not represent given class: " + clazz.getSimpleName());
    }

    public static LocalDate ld(final int year, final int monthOfYear, final int dayOfMonth) {
        return new LocalDate(year, monthOfYear, dayOfMonth);
    }

    public static final DateTime dt(final LocalDate date) {
        return dt(date, 0);
    }

    public static final DateTime dt(final LocalDate date, final int hourOfDay) {
        return dt(date, hourOfDay, 0);
    }

    public static final DateTime dt(final LocalDate date, final int hourOfDay, final int minuteOfHour) {
        return dt(date, hourOfDay, minuteOfHour, 0);
    }

    public static DateTime dt(
            final LocalDate day, final int hourOfDay, final int minuteOfHour, final int secondOfMinute) {

        return day.toDateTime(new LocalTime(hourOfDay, minuteOfHour, secondOfMinute), Constants.DEFAULT_TIMEZONE);
    }

    public static Interval i(final DateTime start, final DateTime end) {
        return new Interval(start, end);
    }

    public static <T, U> Tuple2<T, U> pair(final T first, final U second) {
        return Tuple.of(first, second);
    }

    public static <E extends Exception, T> Consumer<T> wrapExceptionExpectation(
            final Class<E> expectedExceptionClass, final Consumer<? super T> consumer) {

        return t -> {
            try {
                consumer.accept(t);

                fail(String.format("Should have thrown %s but did not", expectedExceptionClass.getSimpleName()));

            } catch (final Exception e) {
                if (!expectedExceptionClass.isAssignableFrom(e.getClass())) {
                    throw new AssertionError(
                            String.format(
                                    "Should have thrown %s, but catched: %s",
                                    expectedExceptionClass.getSimpleName(),
                                    e.getClass().getSimpleName()),
                            e);
                }
            }
        };
    }

    public static <T> Consumer<T> expectNPE(final Consumer<? super T> consumer) {
        return wrapExceptionExpectation(NullPointerException.class, consumer);
    }

    public static <T> Consumer<T> expectIllegalArgumentException(final Consumer<? super T> consumer) {
        return wrapExceptionExpectation(IllegalArgumentException.class, consumer);
    }

    public static <T> Consumer<T> expectValidationException(final Consumer<? super T> consumer) {
        return wrapExceptionExpectation(ValidationException.class, consumer);
    }

    public static <T> Consumer<T> expectRevisionConflictException(final Consumer<? super T> consumer) {
        return wrapExceptionExpectation(RevisionConflictException.class, consumer);
    }

    public static <T> List<T> createList(final int count, final Supplier<T> supplier) {
        Preconditions.checkArgument(count >= 0, "count must be >= 0");
        Objects.requireNonNull(supplier);

        final List<T> ret = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            ret.add(supplier.get());
        }

        return ret;
    }

    public static Executor times(final int count) {
        Preconditions.checkArgument(count >= 0, "count must be >= 0");

        return command -> {
            for (int i = 0; i < count; i++) {
                command.run();
            }
        };
    }

    public interface Executor {
        void run(Runnable command);
    }

    private TestUtils() {
        throw new AssertionError();
    }

}
