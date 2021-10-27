package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.FieldPresence;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import io.vavr.control.Option;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.emptySet;

public class ObservationFieldValidator {

    private final ObservationContextSensitiveFields ctxFields;
    private final boolean userHasCarnivoreAuthority;

    private final Map<String, Required> staticBaseFields;
    private final Map<String, Required> staticSpecimenFields;
    private final Map<String, DynamicObservationFieldPresence> dynamicBaseFields;
    private final Map<String, DynamicObservationFieldPresence> dynamicSpecimenFields;

    private final EnumSet<ObservedGameAge> allowedGameAges;
    private final EnumSet<ObservedGameState> allowedGameStates;
    private final EnumSet<GameMarking> allowedGameMarkings;

    public ObservationFieldValidator(@Nonnull final ObservationBaseFields baseFields,
                                     @Nonnull final ObservationContextSensitiveFields ctxFields,
                                     final boolean userHasCarnivoreAuthority) {

        checkArgument(baseFields.getMetadataVersion() == ctxFields.getMetadataVersion(), "Metadata version mismatch");

        Objects.requireNonNull(baseFields, "baseFields is null");
        this.ctxFields = Objects.requireNonNull(ctxFields, "ctxFields is null");
        this.userHasCarnivoreAuthority = userHasCarnivoreAuthority;

        this.staticBaseFields = ObservationFieldRequirements.getStaticBaseFields(baseFields, ctxFields);
        this.dynamicBaseFields = ObservationFieldRequirements.getDynamicBaseFields(ctxFields);
        this.staticSpecimenFields = ObservationFieldRequirements.getStaticSpecimenFields(ctxFields);
        this.dynamicSpecimenFields = ObservationFieldRequirements.getDynamicSpecimenFields(ctxFields);

        this.allowedGameAges = ctxFields.getAllowedGameAges();
        this.allowedGameStates = ctxFields.getAllowedGameStates();
        this.allowedGameMarkings = ctxFields.getAllowedGameMarkings();
    }

    public ObservationContextSensitiveFields getContextSensitiveFields() {
        return ctxFields;
    }

    /**
     * Throws RuntimeException if any field of observation or its specimen does
     * not conform to requirements.
     *
     * @param observation
     *            observation object containing fields to be validated
     * @param specimens
     *            optional list of specimen objects whose fields are validated
     */
    public void validate(@Nonnull final Object observation, @Nonnull final Optional<List<?>> specimens) {
        validate(observation, specimens, emptySet(), emptySet());
    }

    /**
     * Throws RuntimeException if any field of observation or its specimen does
     * not conform to requirements.
     *
     * @param observation
     *            observation object containing fields to be validated
     * @param specimens
     *            optional list of specimen objects whose fields are validated
     * @param namesOfExcludedBaseFields
     *            set of field names for which observation validation should be
     *            skipped
     * @param namesOfExcludedSpecimenFields
     *            set of field names for which observation specimen validation
     *            should be skipped
     */
    public void validate(@Nonnull final Object observation,
                         @Nonnull final Optional<List<?>> specimens,
                         @Nonnull final Set<String> namesOfExcludedBaseFields,
                         @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

        Objects.requireNonNull(observation, "observation is null");
        Objects.requireNonNull(specimens, "specimens is null");
        Objects.requireNonNull(namesOfExcludedBaseFields, "namesOfExcludedBaseFields is null");
        Objects.requireNonNull(namesOfExcludedSpecimenFields, "namesOfExcludedSpecimenFields is null");

        filterBaseFields(namesOfExcludedBaseFields).forEach((name, fieldPresence) -> {
            assertBaseFieldValue(observation, name, fieldPresence);
        });

        specimens.ifPresent(list -> {

            final Map<String, FieldPresence> specimenFields = filterSpecimenFields(namesOfExcludedSpecimenFields);

            final AtomicInteger specimenIndex = new AtomicInteger(0);

            list.forEach(specimen -> specimenFields.forEach((name, fieldPresence) -> {

                assertSpecimenFieldValue(specimen, specimenIndex.getAndIncrement(), name, fieldPresence);

                final Object age = getValueOfField(specimen, ObservationFieldRequirements.FIELD_AGE);
                assertValidity(age == null || allowedGameAges.contains(age),
                        withinContext(format("ObservedGameAge '%s' not legal", age)));

                final Object state = getValueOfField(specimen, ObservationFieldRequirements.FIELD_STATE);
                assertValidity(state == null || allowedGameStates.contains(state),
                        withinContext(format("ObservedGameState '%s' not legal", state)));

                final Object marking = getValueOfField(specimen, ObservationFieldRequirements.FIELD_MARKING);
                assertValidity(marking == null || allowedGameMarkings.contains(marking),
                        withinContext(format("GameMarking '%s' not legal", marking)));
            }));
        });
    }

    /**
     * Nullifies fields that are not legal according to metadata rules.
     *
     * @param observation
     *            observation object containing fields to be examined and
     *            nullified
     * @param specimens
     *            optional list of specimen objects whose fields may need to be
     *            nullified
     * @param namesOfExcludedBaseFields
     *            set of field names for which observation validation should be
     *            skipped
     * @param namesOfExcludedSpecimenFields
     *            set of field names for which observation specimen validation
     *            should be skipped
     */
    public void nullifyIllegalFields(@Nonnull final Object observation,
                                     @Nonnull final Optional<List<?>> specimens,
                                     @Nonnull final Set<String> namesOfExcludedBaseFields,
                                     @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

        Objects.requireNonNull(observation, "observation is null");
        Objects.requireNonNull(specimens, "specimens is null");
        Objects.requireNonNull(namesOfExcludedBaseFields, "namesOfExcludedBaseFields is null");
        Objects.requireNonNull(namesOfExcludedSpecimenFields, "namesOfExcludedSpecimenFields is null");

        filterBaseFields(namesOfExcludedBaseFields).forEach((fieldName, fieldPresence) -> {
            nullifyFieldIfIllegal(observation, fieldName, fieldPresence);
        });

        specimens.ifPresent(list -> {

            final Map<String, FieldPresence> specimenFields = filterSpecimenFields(namesOfExcludedSpecimenFields);

            list.forEach(specimen -> specimenFields.forEach((fieldName, fieldPresence) -> {
                nullifyFieldIfIllegal(specimen, fieldName, fieldPresence);
            }));
        });
    }

    private Map<String, FieldPresence> filterBaseFields(@Nonnull final Set<String> namesOfExcludedBaseFields) {
        final Map<String, FieldPresence> ret = new HashMap<>(staticBaseFields.size() + dynamicBaseFields.size());

        staticBaseFields.forEach((name, req) -> {
            if (!namesOfExcludedBaseFields.contains(name)) {
                ret.put(name, req);
            }
        });

        dynamicBaseFields.forEach((name, req) -> {
            if (!namesOfExcludedBaseFields.contains(name)) {
                ret.put(name, req.toSimpleFieldPresence(userHasCarnivoreAuthority));
            }
        });

        return ret;
    }

    private Map<String, FieldPresence> filterSpecimenFields(@Nonnull final Set<String> namesOfExcludedSpecimenFields) {
        final Map<String, FieldPresence> ret =
                new HashMap<>(staticSpecimenFields.size() + dynamicSpecimenFields.size());

        staticSpecimenFields.forEach((name, req) -> {
            if (!namesOfExcludedSpecimenFields.contains(name)) {
                ret.put(name, req);
            }
        });

        dynamicSpecimenFields.forEach((name, req) -> {
            if (!namesOfExcludedSpecimenFields.contains(name)) {
                ret.put(name, req.toSimpleFieldPresence(userHasCarnivoreAuthority));
            }
        });

        return ret;
    }

    private static Object getValueOfField(@Nonnull final Object obj, @Nonnull final String fieldName) {
        return getValueOfField(obj, findField(obj, fieldName));
    }

    private static Object getValueOfField(@Nonnull final Object obj, @Nullable final Field field) {
        return field == null ? null : ReflectionUtils.getField(field, obj);
    }

    private static Field findField(@Nonnull final Object obj, @Nonnull final String fieldName) {
        return Option.of(ReflectionUtils.findField(obj.getClass(), fieldName))
                .peek(f -> f.setAccessible(true))
                .getOrElse((Field) null);
    }

    private static void assertValidity(final boolean expression, @Nullable final String errorMessage) {
        if (!expression) {
            throw new ObservationContextSensitiveFieldsNotFoundException(errorMessage);
        }
    }

    private void assertBaseFieldValue(final Object observation,
                                      final String fieldName,
                                      final FieldPresence fieldPresence) {

        fieldPresence.assertValuePresence(getValueOfField(observation, fieldName),
                () -> requiredFieldMissing(fieldName),
                () -> prohibitedFieldFound(fieldName));
    }

    private void assertSpecimenFieldValue(final Object specimen,
                                          final int specimenIndex,
                                          final String fieldName,
                                          final FieldPresence fieldPresence) {

        fieldPresence.assertValuePresence(getValueOfField(specimen, fieldName),
                () -> requiredFieldMissing(formatSpecimenFieldName(specimenIndex, fieldName)),
                () -> prohibitedFieldFound(formatSpecimenFieldName(specimenIndex, fieldName)));
    }

    private static void nullifyFieldIfIllegal(final Object object,
                                              final String fieldName,
                                              final FieldPresence fieldPresence) {

        if (fieldPresence.nullValueRequired()) {

            final Field field = findField(object, fieldName);

            if (getValueOfField(object, field) != null) {
                setFieldNull(object, field);
            }
        }
    }

    private static void setFieldNull(final Object object, final Field field) {
        try {
            field.set(object, null);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String requiredFieldMissing(final String fieldName) {
        return withinContext(format("Required field '%s' missing", fieldName));
    }

    private String prohibitedFieldFound(final String fieldName) {
        return withinContext(format("Illegal field '%s' found", fieldName));
    }

    private static String formatSpecimenFieldName(final int index, final String fieldName) {
        return format("specimens[%d].%s", index, fieldName);
    }

    private String withinContext(final String msg) {
        return msg + " within observation context: " + formatContextAsString();
    }

    private String formatContextAsString() {
        return format("{ gameSpeciesCode: %d, observationCategory: %s, observationType: %s, isCarnivoreAuthority: %s, metadataVersion: %d }",
                ctxFields.getSpecies().getOfficialCode(),
                ctxFields.getObservationCategory(),
                ctxFields.getObservationType().name(),
                userHasCarnivoreAuthority,
                ctxFields.getMetadataVersion());
    }
}
