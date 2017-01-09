package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.Required;

import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptySet;

public class ObservationFieldRequirements implements Serializable {

    protected static Field getField(final Object obj, final String fieldName) {
        final Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        field.setAccessible(true);
        return field;
    }

    protected static Object getFieldValue(final Object obj, final Field field) {
        return ReflectionUtils.getField(field, obj);
    }

    protected static Object getFieldValue(final Object obj, final String fieldName) {
        return getFieldValue(obj, getField(obj, fieldName));
    }

    protected static String formatSpecimenFieldName(final String fieldName, final int specimenIndex) {
        return String.format("specimen[%d].%s", specimenIndex, fieldName);
    }

    private final HashMap<String, Required> baseFields = new HashMap<>();
    private final HashMap<String, Required> specimenFields = new HashMap<>();

    public ObservationFieldRequirements(
            @Nonnull final Set<String> baseFieldNames, @Nonnull final Set<String> specimenFieldNames) {

        Objects.requireNonNull(baseFieldNames);
        Objects.requireNonNull(specimenFieldNames);

        baseFieldNames.forEach(fieldName -> baseFields.put(fieldName, Required.NO));
        specimenFieldNames.forEach(fieldName -> specimenFields.put(fieldName, Required.NO));
    }

    public void assertAllFieldRequirements(
            @Nonnull final Object observation, @Nonnull final Optional<List<? extends Object>> specimens) {

        assertAllFieldRequirements(observation, specimens, emptySet(), emptySet());
    }

    public void assertAllFieldRequirements(
            @Nonnull final Object observation,
            @Nonnull final Optional<List<? extends Object>> specimens,
            @Nonnull final Set<String> namesOfExcludedObservationFields,
            @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

        Objects.requireNonNull(observation);
        Objects.requireNonNull(specimens);
        Objects.requireNonNull(namesOfExcludedObservationFields);
        Objects.requireNonNull(namesOfExcludedSpecimenFields);

        baseFields.forEach((name, requirement) -> {
            if (!namesOfExcludedObservationFields.contains(name)) {
                assertBaseFieldValue(observation, name, requirement);
            }
        });

        specimens.ifPresent(list -> {
            final AtomicInteger specimenIndex = new AtomicInteger(0);

            list.forEach(specimen -> specimenFields.forEach((name, requirement) -> {
                if (!namesOfExcludedSpecimenFields.contains(name)) {
                    assertSpecimenFieldValue(specimen, specimenIndex.getAndIncrement(), name, requirement);
                }
            }));
        });
    }

    public void nullifyProhibitedFields(
            @Nonnull final Object observation,
            @Nonnull final Optional<List<? extends Object>> specimens,
            @Nonnull final Set<String> namesOfExcludedObservationFields,
            @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

        Objects.requireNonNull(observation);
        Objects.requireNonNull(specimens);
        Objects.requireNonNull(namesOfExcludedObservationFields);
        Objects.requireNonNull(namesOfExcludedSpecimenFields);

        baseFields.forEach((name, requirement) -> {
            if (!namesOfExcludedObservationFields.contains(name)) {
                nullifyFieldIfProhibited(observation, name, requirement);
            }
        });

        specimens.ifPresent(list -> list.forEach(specimen -> specimenFields.forEach((name, requirement) -> {
            if (!namesOfExcludedSpecimenFields.contains(name)) {
                nullifyFieldIfProhibited(specimen, name, requirement);
            }
        })));
    }

    // For compacting serialized output.
    public void removeDenyingFieldRequirements() {
        for (final Iterator<Map.Entry<String, Required>> it = getBaseFields().entrySet().iterator(); it.hasNext();) {
            if (it.next().getValue() == Required.NO) {
                it.remove();
            }
        }

        for (final Iterator<Map.Entry<String, Required>> it = getSpecimenFields().entrySet().iterator(); it
                .hasNext();) {

            if (it.next().getValue() == Required.NO) {
                it.remove();
            }
        }
    }

    protected void assertBaseFieldValue(final Object observation, final String fieldName, final Required requirement) {
        requirement.assertValue(getFieldValue(observation, fieldName), fieldName);
    }

    protected void assertSpecimenFieldValue(
            final Object specimen, final int specimenIndex, final String fieldName, final Required requirement) {

        requirement.assertValue(getFieldValue(specimen, fieldName), formatSpecimenFieldName(fieldName, specimenIndex));
    }

    protected void nullifyFieldIfProhibited(final Object object, final String fieldName, final Required requirement) {
        final Field field = getField(object, fieldName);

        if (!requirement.isAllowedField() && getFieldValue(object, field) != null) {
            setFieldNull(object, field);
        }
    }

    protected void setFieldNull(final Object object, final Field field) {
        try {
            field.set(object, null);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Accessors -->

    public Map<String, Required> getBaseFields() {
        return baseFields;
    }

    public Map<String, Required> getSpecimenFields() {
        return specimenFields;
    }

}
