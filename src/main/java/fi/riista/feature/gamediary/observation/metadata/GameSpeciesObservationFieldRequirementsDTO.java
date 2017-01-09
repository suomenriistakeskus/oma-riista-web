package fi.riista.feature.gamediary.observation.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Sets;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@JsonPropertyOrder({ "gameSpeciesCode", "baseFields", "specimenFields", "contextSensitiveFieldSets" })
public class GameSpeciesObservationFieldRequirementsDTO extends ObservationFieldRequirements {

    @JsonPropertyOrder({ "withinMooseHunting", "type", "baseFields", "specimenFields", "allowedAges", "allowedStates", "allowedMarkings" })
    public class ContextSensitiveFieldSetDTO extends ObservationFieldRequirements {

        // Observation field names
        private static final String FIELD_AMOUNT = "amount";
        private static final String FIELD_MOOSELIKE_MALE_AMOUNT = "mooselikeMaleAmount";
        private static final String FIELD_MOOSELIKE_FEMALE_AMOUNT = "mooselikeFemaleAmount";
        private static final String FIELD_MOOSELIKE_FEMALE_1CALF_AMOUNT = "mooselikeFemale1CalfAmount";
        private static final String FIELD_MOOSELIKE_FEMALE_2CALFS_AMOUNT = "mooselikeFemale2CalfsAmount";
        private static final String FIELD_MOOSELIKE_FEMALE_3CALFS_AMOUNT = "mooselikeFemale3CalfsAmount";
        private static final String FIELD_MOOSELIKE_FEMALE_4CALFS_AMOUNT = "mooselikeFemale4CalfsAmount";
        private static final String FIELD_MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT = "mooselikeUnknownSpecimenAmount";

        // Observation specimen field names
        private static final String FIELD_AGE = "age";
        private static final String FIELD_GENDER = "gender";
        private static final String FIELD_STATE = "state";
        private static final String FIELD_MARKING = "marking";

        private final boolean withinMooseHunting;

        private final ObservationType type;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private EnumSet<ObservedGameAge> allowedAges = EnumSet.noneOf(ObservedGameAge.class);

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private EnumSet<ObservedGameState> allowedStates = EnumSet.noneOf(ObservedGameState.class);

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private EnumSet<GameMarking> allowedMarkings = EnumSet.noneOf(GameMarking.class);

        public ContextSensitiveFieldSetDTO(final boolean withinMooseHunting, final ObservationType type) {
            super(singleton(FIELD_AMOUNT), Sets.newHashSet(FIELD_AGE, FIELD_GENDER, FIELD_STATE, FIELD_MARKING));

            this.withinMooseHunting = withinMooseHunting;
            this.type = type;
        }

        @Override
        public void assertAllFieldRequirements(
                @Nonnull final Object observation, @Nonnull final Optional<List<?>> specimens) {

            super.assertAllFieldRequirements(observation, specimens);
        }

        @Override
        public void assertAllFieldRequirements(
                @Nonnull final Object observation,
                @Nonnull final Optional<List<?>> specimens,
                @Nonnull final Set<String> namesOfExcludedObservationFields,
                @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

            super.assertAllFieldRequirements(
                    observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);

            specimens.ifPresent(list -> list.forEach(specimen -> {
                final Object age = getFieldValue(specimen, FIELD_AGE);
                assertValidity(
                        age == null || getAllowedAges().contains(age),
                        String.format(
                                "ObservedGameAge '%s' not legal within context %s", age, getParametersAsString()));

                final Object state = getFieldValue(specimen, FIELD_STATE);
                assertValidity(
                        state == null || getAllowedStates().contains(state),
                        String.format(
                                "ObservedGameState '%s' not legal within context %s", state, getParametersAsString()));

                final Object marking = getFieldValue(specimen, FIELD_MARKING);
                assertValidity(
                        marking == null || getAllowedMarkings().contains(marking),
                        String.format(
                                "GameMarking '%s' not legal within context %s", marking, getParametersAsString()));
            }));
        }

        @Override
        public void nullifyProhibitedFields(
                @Nonnull final Object observation,
                @Nonnull final Optional<List<?>> specimens,
                @Nonnull final Set<String> namesOfExcludedObservationFields,
                @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

            super.nullifyProhibitedFields(
                    observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);

            specimens.ifPresent(list -> list.forEach(specimen -> {
                final Field ageField = getField(specimen, FIELD_AGE);
                final Object age = getFieldValue(specimen, ageField);

                if (age != null && !getAllowedAges().contains(age)) {
                    setFieldNull(specimen, ageField);
                }

                final Field stateField = getField(specimen, FIELD_STATE);
                final Object state = getFieldValue(specimen, stateField);

                if (state != null && !getAllowedStates().contains(state)) {
                    setFieldNull(specimen, stateField);
                }

                final Field markingField = getField(specimen, FIELD_MARKING);
                final Object marking = getFieldValue(specimen, markingField);

                if (marking != null && !getAllowedMarkings().contains(marking)) {
                    setFieldNull(specimen, markingField);
                }
            }));
        }

        @Override
        protected void assertBaseFieldValue(
                final Object observation, final String fieldName, final Required requirement) {

            requirement.assertValue(
                    getFieldValue(observation, fieldName),
                    () -> String.format(
                            "Required observation field '%s' missing within context %s",
                            fieldName, getParametersAsString()),
                    () -> String.format(
                            "Prohibited observation field '%s' found within context %s",
                            fieldName, getParametersAsString()));
        }

        @Override
        protected void assertSpecimenFieldValue(
                final Object specimen, final int specimenIndex, final String fieldName, final Required requirement) {

            final String specimenFieldName = formatSpecimenFieldName(fieldName, specimenIndex);

            requirement.assertValue(
                    getFieldValue(specimen, fieldName),
                    () -> String.format(
                            "Required observation specimen field '%s' missing within context %s",
                            specimenFieldName, getParametersAsString()),
                    () -> String.format(
                            "Prohibited observation specimen field '%s' found within context %s",
                            specimenFieldName, getParametersAsString()));
        }

        protected String getParametersAsString() {
            return formatParameters(gameSpeciesCode, this.withinMooseHunting, this.type);
        }

        // Accessors/mutators -->

        public boolean isWithinMooseHunting() {
            return withinMooseHunting;
        }

        public ObservationType getType() {
            return type;
        }

        public Required getAmount() {
            return getBaseFields().get(FIELD_AMOUNT);
        }

        public void setAmount(final Required amount) {
            getBaseFields().put(FIELD_AMOUNT, amount);
        }

        public Required getMooselikeMaleAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_MALE_AMOUNT);
        }

        public void setMooselikeMaleAmount(final Required mooselikeMaleAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_MALE_AMOUNT, mooselikeMaleAmount);
        }

        public Required getMooselikeFemaleAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_FEMALE_AMOUNT);
        }

        public void setMooselikeFemaleAmount(final Required mooselikeFemaleAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_FEMALE_AMOUNT, mooselikeFemaleAmount);
        }

        public Required getMooselikeFemale1CalfAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_FEMALE_1CALF_AMOUNT);
        }

        public void setMooselikeFemale1CalfAmount(final Required mooselikeFemale1CalfAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_FEMALE_1CALF_AMOUNT, mooselikeFemale1CalfAmount);
        }

        public Required getMooselikeFemale2CalfsAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_FEMALE_2CALFS_AMOUNT);
        }

        public void setMooselikeFemale2CalfsAmount(final Required mooselikeFemale2CalfsAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_FEMALE_2CALFS_AMOUNT, mooselikeFemale2CalfsAmount);
        }

        public Required getMooselikeFemale3CalfsAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_FEMALE_3CALFS_AMOUNT);
        }

        public void setMooselikeFemale3CalfsAmount(final Required mooselikeFemale3CalfsAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_FEMALE_3CALFS_AMOUNT, mooselikeFemale3CalfsAmount);
        }

        public Required getMooselikeFemale4CalfsAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_FEMALE_4CALFS_AMOUNT);
        }

        public void setMooselikeFemale4CalfsAmount(final Required mooselikeFemale4CalfsAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_FEMALE_4CALFS_AMOUNT, mooselikeFemale4CalfsAmount);
        }

        public Required getMooselikeUnknownSpecimenAmount() {
            return getBaseFields().get(FIELD_MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT);
        }

        public void setMooselikeUnknownSpecimenAmount(final Required mooselikeUnknownSpecimenAmount) {
            getBaseFields().put(FIELD_MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT, mooselikeUnknownSpecimenAmount);
        }

        public Required getAge() {
            return getSpecimenFields().get(FIELD_AGE);
        }

        public void setAge(final Required age) {
            getSpecimenFields().put(FIELD_AGE, age);
        }

        public Required getGender() {
            return getSpecimenFields().get(FIELD_GENDER);
        }

        public void setGender(final Required gender) {
            getSpecimenFields().put(FIELD_GENDER, gender);
        }

        public Required getState() {
            return getSpecimenFields().get(FIELD_STATE);
        }

        public void setState(final Required state) {
            getSpecimenFields().put(FIELD_STATE, state);
        }

        public Required getMarking() {
            return getSpecimenFields().get(FIELD_MARKING);
        }

        public void setMarking(final Required marking) {
            getSpecimenFields().put(FIELD_MARKING, marking);
        }

        public EnumSet<ObservedGameAge> getAllowedAges() {
            return allowedAges;
        }

        public void setAllowedAges(final EnumSet<ObservedGameAge> allowedAges) {
            this.allowedAges = allowedAges;
        }

        public EnumSet<ObservedGameState> getAllowedStates() {
            return allowedStates;
        }

        public void setAllowedStates(final EnumSet<ObservedGameState> allowedStates) {
            this.allowedStates = allowedStates;
        }

        public EnumSet<GameMarking> getAllowedMarkings() {
            return allowedMarkings;
        }

        public void setAllowedMarkings(final EnumSet<GameMarking> allowedMarkings) {
            this.allowedMarkings = allowedMarkings;
        }
    }

    private static final String FIELD_WITHIN_MOOSE_HUNTING = "withinMooseHunting";

    protected static void assertValidity(final boolean expression, @Nullable final String errorMessage) {
        if (!expression) {
            throw new ObservationFieldValidationException(errorMessage);
        }
    }

    protected static final String formatParameters(
            final int gameSpeciesCode, final boolean withinMooseHunting, final ObservationType observationType) {

        return String.format("{ gameSpeciesCode: %d, withinMooseHunting: %s, observationType: %s }",
                gameSpeciesCode, withinMooseHunting, observationType == null ? null : observationType.name());
    }

    private final int gameSpeciesCode;

    private final List<ContextSensitiveFieldSetDTO> contextSensitiveFieldSets = new ArrayList<>();

    public GameSpeciesObservationFieldRequirementsDTO(final int gameSpeciesCode) {
        super(singleton(FIELD_WITHIN_MOOSE_HUNTING), emptySet());

        this.gameSpeciesCode = gameSpeciesCode;
    }

    public void assertAllFieldRequirements(
            @Nonnull final CanIdentifyObservationContextSensitiveFields observation,
            @Nonnull final Optional<List<?>> specimens) {

        super.assertAllFieldRequirements(observation, specimens);
    }

    public void assertAllFieldRequirements(
            @Nonnull final CanIdentifyObservationContextSensitiveFields observation,
            @Nonnull final Optional<List<?>> specimens,
            @Nonnull final Set<String> namesOfExcludedObservationFields,
            @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

        super.assertAllFieldRequirements(
                observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);

        findContextSensitiveFieldSet(observation).assertAllFieldRequirements(
                observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);
    }

    public void nullifyProhibitedFields(
            @Nonnull final CanIdentifyObservationContextSensitiveFields observation,
            @Nonnull final Optional<List<?>> specimens,
            @Nonnull final Set<String> namesOfExcludedObservationFields,
            @Nonnull final Set<String> namesOfExcludedSpecimenFields) {

        super.nullifyProhibitedFields(
                observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);

        findContextSensitiveFieldSet(observation).nullifyProhibitedFields(
                observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);
    }

    @Override
    public void removeDenyingFieldRequirements() {
        super.removeDenyingFieldRequirements();
        contextSensitiveFieldSets.forEach(ContextSensitiveFieldSetDTO::removeDenyingFieldRequirements);
    }

    @Override
    protected void assertBaseFieldValue(final Object observation, final String fieldName, final Required requirement) {
        requirement.assertValue(
                getFieldValue(observation, fieldName),
                () -> String.format(
                        "Required observation field '%s' missing for game species code %d", fieldName, gameSpeciesCode),
                () -> String.format(
                        "Prohibited observation field '%s' found for game species code %d",
                        fieldName, gameSpeciesCode));
    }

    @Override
    protected void assertSpecimenFieldValue(
            final Object specimen, final int specimenIndex, final String fieldName, final Required requirement) {

        final String specimenFieldName = formatSpecimenFieldName(fieldName, specimenIndex);

        requirement.assertValue(
                getFieldValue(specimen, fieldName),
                () -> String.format(
                        "Required observation specimen field '%s' missing for game species code %d",
                        specimenFieldName, gameSpeciesCode),
                () -> String.format(
                        "Prohibited observation specimen field '%s' found for game species code %d",
                        specimenFieldName, gameSpeciesCode));
    }

    private ContextSensitiveFieldSetDTO findContextSensitiveFieldSet(
            final CanIdentifyObservationContextSensitiveFields criteria) {

        final boolean withinMooseHunting = criteria.observedWithinMooseHunting();
        final ObservationType observationType = criteria.getObservationType();

        return contextSensitiveFieldSets.stream()
                .filter(validationDto -> {
                    return validationDto.isWithinMooseHunting() == withinMooseHunting
                            && validationDto.getType() == observationType;
                })
                .findFirst()
                .orElseThrow(() -> new ObservationFieldValidationException(String.format(
                        "Cannot resolve context sensitive field set for context %s",
                        formatParameters(gameSpeciesCode, withinMooseHunting, observationType))));
    }

    // Accessors/mutators -->

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public Required getWithinMooseHunting() {
        return getBaseFields().get(FIELD_WITHIN_MOOSE_HUNTING);
    }

    public void setWithinMooseHunting(final Required withinMooseHunting) {
        getBaseFields().put(FIELD_WITHIN_MOOSE_HUNTING, withinMooseHunting);
    }

    public List<ContextSensitiveFieldSetDTO> getContextSensitiveFieldSets() {
        return contextSensitiveFieldSets;
    }

}
