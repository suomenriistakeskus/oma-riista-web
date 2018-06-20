package fi.riista.feature.common.entity;

import com.google.common.base.Preconditions;
import fi.riista.validation.Validators;
import org.apache.commons.lang.StringUtils;

import javax.persistence.AttributeConverter;

public class FinnishBusinessIdConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        return StringUtils.isBlank(attribute) ? null : formatAndValidate(attribute);
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        return StringUtils.isBlank(dbData) ? null : formatAndValidate(dbData);
    }

    private static String formatAndValidate(final String input) {
        final String formatted = format(input);
        Preconditions.checkArgument(Validators.isValidBusinessId(formatted), "Invalid business id:" + input);
        return formatted;
    }

    public static String format(final String input) {
        if (input == null) {
            return input;
        }
        final String onlyNumbers = input.replaceAll("[^0-9]", "");
        if (onlyNumbers.length() < 8) {
            return input;
        }
        return String.format("%s-%s",
                onlyNumbers.substring(0, onlyNumbers.length() - 1),
                onlyNumbers.substring(onlyNumbers.length() - 1));
    }
}
