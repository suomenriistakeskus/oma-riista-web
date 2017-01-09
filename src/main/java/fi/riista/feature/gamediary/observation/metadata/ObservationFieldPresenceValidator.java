package fi.riista.feature.gamediary.observation.metadata;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ObservationFieldPresenceValidator {

    /**
     * Throws RuntimeException if any field of observation or its specimen does
     * not conform to requirements.
     *
     * @param observation
     *            observation object containing fields to be validated
     * @param specimens
     *            optional list of specimen objects whose fields are validated
     */
    void validate(@Nonnull CanIdentifyObservationContextSensitiveFields observation, @Nonnull Optional<List<?>> specimens);

    /**
     * Throws RuntimeException if any field of observation or its specimen does
     * not conform to requirements.
     *
     * @param observation
     *            observation object containing fields to be validated
     * @param specimens
     *            optional list of specimen objects whose fields are validated
     * @param namesOfExcludedObservationFields
     *            set of field names for which observation validation should be
     *            skipped
     * @param namesOfExcludedSpecimenFields
     *            set of field names for which observation specimen validation
     *            should be skipped
     */
    void validate(
            @Nonnull CanIdentifyObservationContextSensitiveFields observation,
            @Nonnull Optional<List<?>> specimens,
            @Nonnull Set<String> namesOfExcludedObservationFields,
            @Nonnull Set<String> namesOfExcludedSpecimenFields);

    /**
     *
     * @param observation
     *            observation object containing fields to be examined
     * @param specimens
     *            optional list of specimen objects whose fields are examined
     * @param namesOfExcludedObservationFields
     *            name set of observation fields that should not be nullified
     * @param namesOfExcludedSpecimenFields
     *            name set of observation specimen fields that should not be
     *            nullified
     */
    void nullifyProhibitedFields(
            @Nonnull CanIdentifyObservationContextSensitiveFields observation,
            @Nonnull Optional<List<?>> specimens,
            @Nonnull Set<String> namesOfExcludedObservationFields,
            @Nonnull Set<String> namesOfExcludedSpecimenFields);

}
