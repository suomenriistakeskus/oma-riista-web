package fi.riista.feature.mail.token;

import fi.riista.feature.common.entity.PersistableEnumConverter;

import javax.persistence.Converter;

@Converter
public class EmailTokenTypeConverter implements PersistableEnumConverter<EmailTokenType> {
}
