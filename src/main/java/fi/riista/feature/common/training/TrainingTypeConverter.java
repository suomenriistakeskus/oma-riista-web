package fi.riista.feature.common.training;

import fi.riista.feature.common.entity.PersistableEnumConverter;

import javax.persistence.Converter;

@Converter
public class TrainingTypeConverter implements PersistableEnumConverter<TrainingType> {
}
