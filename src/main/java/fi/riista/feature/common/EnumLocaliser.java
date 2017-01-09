package fi.riista.feature.common;

import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.Localiser;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.Locale;

@Component
public class EnumLocaliser extends Localiser {

    @Inject
    public EnumLocaliser(@Nonnull final MessageSource messageSource) {
        super(messageSource);
    }

    public EnumLocaliser(@Nonnull final MessageSource messageSource, @Nullable final Locale locale) {
        super(messageSource, locale);
    }

    @Nullable
    public static <E extends Enum<?>> String resourceKey(@Nullable final E enumValue) {
        if (enumValue == null) {
            return null;
        }

        final Class<?> enumClass = enumValue.getClass();
        final Class<?> outerClass = enumClass.getDeclaringClass();

        return outerClass != null
                ? String.format("%s.%s.%s", outerClass.getSimpleName(), enumClass.getSimpleName(), enumValue.name())
                : String.format("%s.%s", enumClass.getSimpleName(), enumValue.name());
    }

    @Nullable
    public <E extends Enum<?>> String getTranslation(@Nullable final E enumValue) {
        return getTranslation(resourceKey(enumValue));
    }

    @Nullable
    public <E extends Enum<E>> String getTranslation(@Nullable final E enumValue, final Locale locale) {
        return getTranslation(resourceKey(enumValue), locale);
    }

    @Nullable
    public <E extends Enum<E>> LocalisedString getLocalisedString(@Nullable final E enumValue) {
        return enumValue != null
                ? LocalisedString.of(getTranslation(enumValue, Locales.FI), getTranslation(enumValue, Locales.SV))
                : null;
    }

}
