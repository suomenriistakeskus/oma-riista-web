package fi.riista.feature.common.entity;

import org.apache.commons.lang.StringUtils;
import org.iban4j.Iban;

import javax.persistence.AttributeConverter;

public class IbanConverter implements AttributeConverter<Iban, String> {

    @Override
    public String convertToDatabaseColumn(final Iban attribute) {
        return attribute != null ? trimAllWhiteSpace(attribute.toString()) : null;
    }

    @Override
    public Iban convertToEntityAttribute(final String dbData) {
        return StringUtils.isNotBlank(dbData) ? Iban.valueOf(trimAllWhiteSpace(dbData)) : null;
    }

    private static String trimAllWhiteSpace(final String value) {
        return value != null ? value.replaceAll(" ", "") : null;
    }
}
