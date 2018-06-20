package fi.riista.feature.common.entity;

import fi.riista.feature.error.ProhibitedFieldFound;
import fi.riista.feature.error.RequiredFieldMissing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface FieldPresence {

    boolean nullValueRequired();

    boolean nonNullValueRequired();

    default boolean isNonNullValueLegal() {
        return !nullValueRequired();
    }

    default boolean isValidValue(@Nullable final Object value) {
        if (nonNullValueRequired()) {
            return value != null;
        } else if (nullValueRequired()) {
            return value == null;
        }
        return true;
    }

    default void assertValuePresence(@Nullable final Object value,
                                     @Nonnull final Supplier<String> errorMessageWhenRequiredFieldMissing,
                                     @Nonnull final Supplier<String> errorMessageWhenProhibitedFieldFound) {

        if (nonNullValueRequired()) {
            RequiredFieldMissing.assertValueIsNotNull(value, errorMessageWhenRequiredFieldMissing);
        } else if (nullValueRequired()) {
            ProhibitedFieldFound.assertValueIsNull(value, errorMessageWhenProhibitedFieldFound);
        }
    }

    default <T> T nullifyIfNeeded(@Nullable final T value, @Nonnull final String fieldName) {
        if (nonNullValueRequired()) {
            RequiredFieldMissing.assertValueIsNotNull(value, fieldName);
            return value;
        }
        return nullValueRequired() ? null : value;
    }
}
