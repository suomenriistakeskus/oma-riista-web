package fi.riista.test;

import com.google.common.base.Joiner;
import fi.riista.feature.huntingclub.moosedatacard.validation.NumericFieldMeta;
import fi.riista.util.F;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.hamcrest.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Ordering.usingToString;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public final class Asserts {

    private static final String DEFAULT_JOINED_ITEM_SEPARATOR = "\n    ";

    public static <T> void assertThat(final T actual, @Nonnull final Matcher<? super T> matcher) {
        org.hamcrest.MatcherAssert.assertThat(actual, matcher);
    }

    public static <T> void assertThat(final T actual, @Nonnull final Matcher<? super T> matcher, final String reason) {
        org.hamcrest.MatcherAssert.assertThat(reason, actual, matcher);
    }

    public static <T> void assertEmpty(@Nonnull final Stream<T> stream, @Nonnull final String onNotEmptyMessage) {
        assertThat(stream.sorted(usingToString()).collect(toList()), is(empty()), onNotEmptyMessage);
    }

    /**
     * Asserts that given collection is empty.
     *
     * @deprecated Since August 2020 {@link org.hamcrest.Matchers#empty()
     *             Matchers.empty} can be used to verify that your collection is
     *             empty.
     */
    @Deprecated
    public static void assertEmpty(@Nonnull final Collection<?> coll) {
        assertThat(coll, is(empty()),
                format("Expected empty collection but had %d elements", coll.size()));
    }

    /**
     * Asserts that given collection is empty.
     *
     * @deprecated Since August 2020 {@link org.hamcrest.Matchers#empty()
     *             Matchers.empty} can be used to verify that your collection is
     *             empty.
     */
    @Deprecated
    public static void assertEmpty(@Nonnull final Collection<?> coll, @Nonnull final String onNotEmptyMessage) {
        assertThat(coll, is(empty()),
                consructErrorMessage(onNotEmptyMessage, coll, DEFAULT_JOINED_ITEM_SEPARATOR));
    }

    public static <T, U> void assertEqualAfterTransformation(@Nonnull final Iterable<T> first,
                                                             @Nonnull final Iterable<T> second,
                                                             @Nonnull final Function<? super T, U> transformation) {

        requireNonNull(first, "first is null");
        requireNonNull(second, "second is null");
        requireNonNull(transformation, "transformation is null");

        final List<U> firstTransformed = F.stream(first).map(transformation).collect(toList());
        final List<U> secondTransformed = F.stream(second).map(transformation).collect(toList());

        assertThat(firstTransformed, is(equalTo(secondTransformed)));
    }

    public static <K, V1, V2> void assertEqualMapsAfterValueTransformation(
            @Nonnull final Map<K, V1> first,
            @Nonnull final Map<K, V1> second,
            @Nonnull final Function<? super V1, V2> valueMapper) {

        requireNonNull(first, "first is null");
        requireNonNull(second, "second is null");
        requireNonNull(valueMapper, "valueMapper is null");

        final HashMap<K, V2> firstTransformed = HashMap.ofAll(first).mapValues(valueMapper);
        final HashMap<K, V2> secondTransformed = HashMap.ofAll(second).mapValues(valueMapper);

        assertThat(firstTransformed, is(equalTo(secondTransformed)));
    }

    public static <T> void assertSuccess(@Nonnull final Try<T> tryObj, @Nonnull final Consumer<T> assertions) {
        if (tryObj.isFailure()) {
            fail("Expected success, but was: " + tryObj.getCause());
        } else {
            assertions.accept(tryObj.get());
        }
    }

    public static <T> void assertSuccess(@Nullable final T expectedValue, @Nonnull final Try<T> tryObj) {
        assertSuccess(tryObj, value -> assertThat(value, is(equalTo(expectedValue))));
    }

    public static <T> void assertFailure(@Nonnull final Try<T> tryObj, @Nonnull final Consumer<Throwable> assertions) {
        if (tryObj.isSuccess()) {
            fail("Expected failure, but was: " + tryObj.get());
        } else {
            assertions.accept(tryObj.getCause());
        }
    }

    public static void assertFailure(@Nonnull final Class<?> expectedThrowableType, @Nonnull final Try<?> tryObj) {
        assertFailure(tryObj, throwable -> assertThat(throwable.getClass(), is(equalTo(expectedThrowableType))));
    }

    public static <T> void assertValid(@Nonnull final Validation<?, T> validation,
                                       @Nonnull final Consumer<T> assertions) {

        if (!validation.isValid()) {
            fail("Validation should have passed, but got: " + validation.getError());
        } else {
            assertions.accept(validation.get());
        }
    }

    public static <T> void assertValidationError(@Nonnull final Validation<String, T> validation,
                                                 @Nonnull final String expectedMessage) {

        validation.peek(t -> fail("Validation should have failed"))
                .swap().peek(errMsg -> assertThat(errMsg, is(equalTo(expectedMessage))));
    }

    public static <T> void assertValidationErrors(@Nonnull final Validation<List<String>, T> validation,
                                                  @Nonnull final String expectedMessage) {

        assertValidationErrors(validation, Collections.singletonList(expectedMessage));
    }

    public static <T> void assertValidationErrors(@Nonnull final Validation<List<String>, T> validation,
                                                  @Nonnull final String... expectedMessages) {

        assertValidationErrors(validation, Arrays.asList(expectedMessages));
    }

    public static <T> void assertValidationErrors(@Nonnull final Validation<List<String>, T> validation,
                                                  @Nonnull final List<String> expectedMessages) {

        validation.peek(t -> fail("Validation should have failed"))
                .swap()
                .peek(errors -> assertThat(errors, is(equalTo(expectedMessages))));
    }

    public static <T, N extends Number & Comparable<N>> void assertNumericFieldValidationError(
            @Nonnull final T object,
            @Nonnull final Function<? super T, Validation<List<String>, T>> validationFunction,
            @Nonnull final NumericFieldMeta<T, N> field) {

        final String onValidMsg =
                format("Validation should have failed when '%s'=%s", field.getNameFinnish(), field.apply(object));

        final Validation<String, Optional<N>> fieldValidation = field.validate(object);

        assertThat(fieldValidation.isInvalid(), is(true), onValidMsg);

        validationFunction.apply(object)
                .peek(t -> fail(onValidMsg))
                .swap()
                .peek(errors -> assertThat(errors, contains(fieldValidation.getError())));
    }

    public static <T> void assertNumericFieldValidationErrors(
            @Nonnull final T object,
            @Nonnull final Function<? super T, Validation<List<String>, T>> validationFunction,
            @Nonnull final Stream<? extends NumericFieldMeta<T, ?>> fields) {

        final List<String> expectedMessages = fields.map(field -> getValidationError(object, field)).collect(toList());

        assertValidationErrors(validationFunction.apply(object), expectedMessages);
    }

    public static <T> String getValidationError(@Nonnull final T object, @Nonnull final NumericFieldMeta<T, ?> field) {
        final Validation<String, ?> validation = field.validate(object);

        assertThat(validation.isInvalid(), is(true),
                format("Validation should have failed when '%s'=%s", field.getNameFinnish(), field.apply(object)));

        return validation.getError();
    }

    private static String consructErrorMessage(
            final String message, final Iterable<?> failingItems, final String separator) {

        requireNonNull(failingItems, "failingItems is null");

        return consructErrorMessage(message, join(failingItems, separator), separator);
    }

    private static String consructErrorMessage(final String message, final String joinedItems, final String separator) {
        requireNonNull(message, "message is null");

        return new StringBuilder(message)
                .append(separator)
                .append(joinedItems)
                .append('\n')
                .toString();
    }

    private static String join(final Iterable<?> parts, final String separator) {
        return Joiner.on(separator).skipNulls().join(parts);
    }

    private Asserts() {
        throw new AssertionError();
    }
}
