package fi.riista.feature.vetuma.entity;

import fi.riista.feature.common.entity.PersistableEnumConverter;

import javax.persistence.Converter;

@Converter
public class VetumaTransactionStatusConverter implements PersistableEnumConverter<VetumaTransactionStatus> {
}
