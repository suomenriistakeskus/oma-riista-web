package fi.riista.feature.common.entity;


import org.apache.commons.lang.StringUtils;

import javax.persistence.AttributeConverter;

public class CreditorReferenceConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        final String str = StringUtils.replace(attribute, " ", "");
        return StringUtils.isBlank(str) ? null : str;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return null;
        }
        return getDelimitedValue(dbData.replace(" ", ""));

    }

    private static String getDelimitedValue(String str) {
        final char delimiter = ' ';
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (i != 0 && i % 5 == 0) {
                buffer.append(delimiter);
            }
            buffer.append(str.charAt(i));
        }
        return buffer.toString();
    }
}
