package fi.riista.feature.common.entity;


import org.apache.commons.lang.StringUtils;
import org.iban4j.Iban;
import org.iban4j.IbanFormat;

import javax.persistence.AttributeConverter;

public class IbanConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return StringUtils.isBlank(attribute) ? null : Iban.valueOf(attribute, IbanFormat.Default).toString();
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return StringUtils.isBlank(dbData) ? null : Iban.valueOf(dbData).toFormattedString();
    }
}
