package fi.riista.feature.account.registration;

import fi.riista.feature.common.entity.PersistableEnumConverter;

import javax.persistence.Converter;

@Converter
public class VetumaTransactionStatusConverter implements PersistableEnumConverter<VetumaTransactionStatus> {
}
