package fi.riista.feature.common.entity;


import org.apache.commons.lang.StringUtils;
import org.iban4j.Bic;

import javax.persistence.AttributeConverter;

public class BicConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return StringUtils.isBlank(attribute) ? null : Bic.valueOf(attribute).toString();
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return StringUtils.isBlank(dbData) ? null : Bic.valueOf(dbData).toString();
    }
}
