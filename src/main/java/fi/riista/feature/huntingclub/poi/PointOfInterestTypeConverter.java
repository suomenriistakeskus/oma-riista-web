package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.common.entity.PersistableEnumConverter;

import javax.persistence.Converter;

@Converter
public class PointOfInterestTypeConverter implements PersistableEnumConverter<PointOfInterestType> {
}
