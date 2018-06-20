package fi.riista.feature.mail;

import com.github.jknack.handlebars.Options;
import fi.riista.util.Locales;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.MessageSource;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import static fi.riista.util.NumberUtils.squareMetersToHectares;

/**
 * This class containts different helpers that extend the functionality of Handlebars templates.
 * <p>
 * Usage in a template: "{{<function name> <parameter1> <parameter2>... [optionA=<parameter>] ...}}"
 */

public class HandlebarsHelperSource {

    private MessageSource source;

    public HandlebarsHelperSource(MessageSource source) {
        this.source = source;
    }

    public CharSequence i18n(String identifier, final Options options) {
        return source.getMessage(
                identifier,
                options.params,
                options.hash("default"),
                Locales.getLocaleByLanguageCode(options.hash("locale")));
    }

    public static CharSequence number(Number input, String format, final Options options) {
        DecimalFormat df = new DecimalFormat(format,
                new DecimalFormatSymbols(Locales.getLocaleByLanguageCode(options.hash("locale"))));
        return df.format(input);
    }

    public static CharSequence toUpperCase(String input) {
        return input.toUpperCase();
    }

    public static CharSequence timestamp(Date input, String format) {
        return DateTimeFormat.forPattern(format).print(input.getTime());
    }

    public static CharSequence enumName(Enum<?> input) {
        return input.getDeclaringClass().getSimpleName() + "." + input.name();
    }

    public static CharSequence boolName(Boolean input) {
        return input ? "Boolean.true" : "Boolean.false";
    }

    public static CharSequence hectaresRounded(double squareMeters) {
        return Long.toString(squareMetersToHectares(squareMeters)) + " ha";
    }
}
