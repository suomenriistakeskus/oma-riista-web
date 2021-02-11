package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.PersistableEnumConverter;

import javax.persistence.Converter;

@Converter
public class ObservationCategoryConverter implements PersistableEnumConverter<ObservationCategory> {
}
