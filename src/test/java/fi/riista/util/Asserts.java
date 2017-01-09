package fi.riista.util;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Joiner;

import fi.riista.feature.huntingclub.moosedatacard.validation.NumericFieldMeta;

import javaslang.collection.HashMap;
import javaslang.control.Try;
import javaslang.control.Validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Asserts {

    private static final String DEFAULT_JOINED_ITEM_SEPARATOR = "\n    ";

    public static <T> void assertEmpty(@Nonnull final Stream<T> stream, @Nonnull final String onNotEmptyMessage) {
        assertEmpty(stream.collect(toList()), onNotEmptyMessage);
    }

    public static void assertEmpty(@Nonnull final Collection<?> coll) {
        Objects.requireNonNull(coll);
        assertTrue(String.format("Expected empty collection but had %d elements", coll.size()), coll.isEmpty());
    }

    public static void assertEmpty(@Nonnull final Collection<?> coll, @Nonnull final String onNotEmptyMessage) {
        assertFalse(consructErrorMessage(
                onNotEmptyMessage, coll, DEFAULT_JOINED_ITEM_SEPARATOR), coll.iterator().hasNext());
    }

    public static <T, U> void assertEqualAfterTransformation(
            @Nonnull final Iterable<T> first,
            @Nonnull final Iterable<T> second,
            @Nonnull final Function<? super T, U> transformation) {

        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        Objects.requireNonNull(transformation, "transformation must not be null");

        assertEquals(
                F.stream(first).map(transformation).collect(toList()),
                F.stream(second).map(transformation).collect(toList()));
    }

    public static <K, V1, V2> void assertEqualMapsAfterValueTransformation(
            @Nonnull final Map<K, V1> first,
            @Nonnull final Map<K, V1> second,
            @Nonnull final Function<? super V1, V2> valueMapper) {

        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        Objects.requireNonNull(valueMapper, "valueMapper must not be null");

        assertEquals(HashMap.ofAll(first).mapValues(valueMapper), HashMap.ofAll(second).mapValues(valueMapper));
    }

    public static <T> void assertSuccess(@Nonnull final Try<T> tryObj, @Nonnull final Consumer<T> assertions) {
        if (tryObj.isFailure()) {
            fail("Expected success, but was: " + tryObj.getCause());
        }
        assertions.accept(tryObj.get());
    }

    public static <T> void assertSuccess(@Nullable final T expectedValue, @Nonnull final Try<T> tryObj) {
        assertSuccess(tryObj, value -> assertEquals(expectedValue, value));
    }

    public static <T> void assertFailure(@Nonnull final Try<T> tryObj, @Nonnull final Consumer<Throwable> assertions) {
        if (tryObj.isSuccess()) {
            fail("Expected failure, but was: " + tryObj.get());
        }
        assertions.accept(tryObj.getCause());
    }

    public static void assertFailure(@Nonnull final Class<?> expectedThrowableType, @Nonnull final Try<?> tryObj) {
        assertFailure(tryObj, throwable -> assertEquals(expectedThrowableType, throwable.getClass()));
    }

    public static <T> void assertValid(
            @Nonnull final Validation<?, T> validation, @Nonnull final Consumer<T> assertions) {

        assertTrue("Validation should have passed", validation.isValid());
        assertions.accept(validation.get());
    }

    public static <T> void assertValidationError(
            @Nonnull final Validation<String, T> validation, @Nonnull final String expectedMessage) {

        validation.peek(t -> fail("Validation should have failed"))
                .swap().peek(errMsg -> assertEquals(expectedMessage, errMsg));
    }

    public static <T> void assertValidationErrors(
            @Nonnull final Validation<List<String>, T> validation, @Nonnull final String expectedMessage) {

        assertValidationErrors(validation, Collections.singletonList(expectedMessage));
    }

    public static <T> void assertValidationErrors(
            @Nonnull final Validation<List<String>, T> validation, @Nonnull final String... expectedMessages) {

        assertValidationErrors(validation, Arrays.asList(expectedMessages));
    }

    public static <T> void assertValidationErrors(
            @Nonnull final Validation<List<String>, T> validation, @Nonnull final List<String> expectedMessages) {

        validation.peek(t -> fail("Validation should have failed"))
                .swap().peek(errors -> assertEquals(expectedMessages, errors));
    }

    public static <T, N extends Number & Comparable<N>> void assertNumericFieldValidationError(
            @Nonnull final T object,
            @Nonnull final Function<? super T, Validation<List<String>, T>> validationFunction,
            @Nonnull final NumericFieldMeta<T, N> field) {

        final String onValidMsg = String.format(
                "Validation should have failed when '%s'=%s", field.getNameFinnish(), field.apply(object));

        final Validation<String, Optional<N>> fieldValidation = field.validate(object);
        assertTrue(onValidMsg, fieldValidation.isInvalid());

        validationFunction.apply(object)
                .peek(t -> fail(onValidMsg))
                .swap().peek(errors -> assertEquals(Collections.singletonList(fieldValidation.getError()), errors));
    }

    public static <T> void assertNumericFieldValidationErrors(
            @Nonnull final T object,
            @Nonnull final Function<? super T, Validation<List<String>, T>> validationFunction,
            @Nonnull final Stream<? extends NumericFieldMeta<T, ?>> fields) {

        final List<String> expectedMessages = fields.map(field -> getValidationError(object, field)).collect(toList());

        assertValidationErrors(validationFunction.apply(object), expectedMessages);
    }

    public static <T> String getValidationError(@Nonnull final T object, @Nonnull final NumericFieldMeta<T, ?> field) {
        final String onValidMsg = String.format(
                "Validation should have failed when '%s'=%s", field.getNameFinnish(), field.apply(object));

        final Validation<String, ?> validation = field.validate(object);
        assertTrue(onValidMsg, validation.isInvalid());

        return validation.getError();
    }

    private static String consructErrorMessage(
            final String message, final Iterable<?> failingItems, final String separator) {

        Objects.requireNonNull(failingItems, "failingItems is null");

        return consructErrorMessage(message, join(failingItems, separator), separator);
    }

    private static String consructErrorMessage(final String message, final String joinedItems, final String separator) {
        Objects.requireNonNull(message, "message is null");

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
