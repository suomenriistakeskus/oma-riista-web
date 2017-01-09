package fi.riista.feature.common.entity;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ValidationException;

import java.util.Objects;
import java.util.function.Supplier;

public enum Required {

    YES, NO, VOLUNTARY;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RequiredFieldMissing extends MessageExposableValidationException {
        public RequiredFieldMissing(final String msg) {
            super(msg);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ProhibitedFieldFound extends MessageExposableValidationException {
        public ProhibitedFieldFound(final String msg) {
            super(msg);
        }
    }

    static String getDefaultMessageForMissingField(final String fieldName) {
        return String.format("Required field missing: %s", fieldName);
    }

    static String getDefaultMessageForProhibitedField(final String fieldName) {
        return String.format("Illegal field found: %s", fieldName);
    }

    public boolean nullValueRequired() {
        return this == NO;
    }

    public boolean nonNullValueRequired() {
        return this == YES;
    }

    public boolean isAllowedField() {
        return !nullValueRequired();
    }

    public <T> T nullifyIfNeeded(@Nullable final T value, @Nonnull final String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        assertFieldNotMissingIfRequired(value, () -> getDefaultMessageForMissingField(fieldName));
        return nullValueRequired() ? null : value;
    }

    public boolean isValidValue(@Nullable final Object value) {
        return this == NO && value == null || this == VOLUNTARY || this == YES && value != null;
    }

    public void assertValue(@Nullable final Object value, @Nonnull final String fieldName) {
        assertFieldNotMissingIfRequired(value, () -> getDefaultMessageForMissingField(fieldName));
        assertFieldNotExistsIfProhibited(value, () -> getDefaultMessageForProhibitedField(fieldName));
    }

    public void assertValue(
            @Nullable final Object value,
            @Nonnull final Supplier<String> errorMessageForRequiredFieldMissing,
            @Nonnull final Supplier<String> errorMessageForProhibitedFieldFound) {

        Objects.requireNonNull(errorMessageForRequiredFieldMissing);
        Objects.requireNonNull(errorMessageForProhibitedFieldFound);

        assertFieldNotMissingIfRequired(value, errorMessageForRequiredFieldMissing);
        assertFieldNotExistsIfProhibited(value, errorMessageForProhibitedFieldFound);
    }

    public Double validateWeight(@Nullable final Double value, @Nonnull final HuntingMethod huntingMethod) {
        return huntingMethod == HuntingMethod.SHOT_BUT_LOST ? null : nullifyIfNeeded(value, "weight");
    }

    public String validateHuntingParty(@Nullable final String value, @Nonnull final HuntingAreaType huntingAreaType) {
        if (huntingAreaType != HuntingAreaType.HUNTING_SOCIETY) {
            return null;
        }

        final String huntingParty = nullifyIfNeeded(value, "huntingParty");

        if (this != NO && StringUtils.isBlank(huntingParty)) {
            throw new ValidationException();
        }

        return huntingParty;
    }

    private void assertFieldNotMissingIfRequired(final Object value, final Supplier<String> errorMessage) {
        if (nonNullValueRequired() && value == null) {
            throw new RequiredFieldMissing(errorMessage.get());
        }
    }

    private void assertFieldNotExistsIfProhibited(final Object value, final Supplier<String> errorMessage) {
        if (nullValueRequired() && value != null) {
            throw new ProhibitedFieldFound(errorMessage.get());
        }
    }

}
