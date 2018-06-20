package fi.riista.feature.common.entity;


import org.apache.commons.lang.StringUtils;
import org.iban4j.Bic;

import javax.persistence.AttributeConverter;

public class BicConverter implements AttributeConverter<Bic, String> {

    @Override
    public String convertToDatabaseColumn(Bic attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public Bic convertToEntityAttribute(String dbData) {
        return StringUtils.isNotBlank(dbData) ? Bic.valueOf(trimAllWhiteSpace(dbData)) : null;
    }

    private static String trimAllWhiteSpace(final String value) {
        return value != null ? value.replaceAll(" ", "") : null;
    }
}
