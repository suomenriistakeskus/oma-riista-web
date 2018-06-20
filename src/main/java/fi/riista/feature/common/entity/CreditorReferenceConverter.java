package fi.riista.feature.common.entity;

import org.apache.commons.lang.StringUtils;

import javax.persistence.AttributeConverter;

public class CreditorReferenceConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        final String str = StringUtils.replace(attribute, " ", "");
        return StringUtils.isBlank(str) ? null : str;
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        return StringUtils.isBlank(dbData) ? null : CreditorReference.getDelimitedValue(dbData);
    }
}
