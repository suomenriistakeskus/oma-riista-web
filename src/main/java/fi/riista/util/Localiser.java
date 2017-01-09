package fi.riista.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class Localiser {

    private final MessageSource messageSource;
    private final Locale locale;

    public static boolean isSwedishLanguageSelected() {
        return Locales.SV_LANG.equals(LocaleContextHolder.getLocale().getLanguage());
    }

    public static String select(@Nonnull final LocalisedString ls) {
        Objects.requireNonNull(ls);

        return select(ls.getFinnish(), ls.getSwedish());
    }

    public static String select(@Nullable final String fi, @Nullable final String sv) {
        return isSwedishLanguageSelected() ? sv : fi;
    }

    public Localiser(@Nonnull final MessageSource messageSource) {
        this(messageSource, null);
    }

    public Localiser(@Nonnull final MessageSource messageSource, @Nullable final Locale locale) {
        this.messageSource = Objects.requireNonNull(messageSource);
        this.locale = locale;
    }

    /**
     * Locale is resolved dynamically using Spring's LocaleContextHolder if not
     * given explicitly as constructor parameter.
     */
    @Nullable
    public String getTranslation(@Nullable final String message) {
        return getTranslation(message, getLocale());
    }

    @Nullable
    public String getTranslation(@Nullable final String message, @Nullable final Locale locale) {
        return message == null ? null : messageSource.getMessage(message, null, locale);
    }

    public final Function<String, String> asFunction() {
        return input -> input == null ? null : getTranslation(input);
    }

    public String getTranslation(final boolean value) {
        return getTranslation(value, getLocale());
    }

    public String getTranslation(final boolean value, @Nullable final Locale locale) {
        return getTranslation(Boolean.valueOf(value), locale);
    }

    @Nullable
    public String getTranslation(@Nullable final Boolean value) {
        return getTranslation(value, getLocale());
    }

    @Nullable
    public String getTranslation(@Nullable final Boolean value, @Nullable final Locale locale) {
        return value == null ? null :
                messageSource.getMessage(String.format("%s.%s", Boolean.class.getSimpleName(), value), null, locale);
    }

    protected Locale getLocale() {
        return locale != null ? locale : LocaleContextHolder.getLocale();
    }

}
