package fi.riista.feature.common.entity;

import fi.riista.util.ClassUtils;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;

import java.util.stream.Stream;

public interface PersistableEnumConverter<E extends PersistableEnum> extends AttributeConverter<E, String> {

    @SuppressWarnings("unchecked")
    default Class<E> getEnumClass() {
        return (Class<E>) ClassUtils.getTypeArgumentOfSuperClass(this, PersistableEnumConverter.class, 0);
    }

    @Override
    default String convertToDatabaseColumn(@Nullable final E enumValue) {
        return enumValue == null ? null : enumValue.getDatabaseValue();
    }

    @Override
    default E convertToEntityAttribute(@Nullable final String dbData) {
        return dbData == null
                ? null
                : Stream.of(getEnumClass().getEnumConstants())
                        .filter(value -> value.getDatabaseValue().equals(dbData))
                        .findFirst()
                        .orElse(null);
    }

}
