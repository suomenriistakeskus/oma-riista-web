package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

import static fi.riista.feature.common.entity.Required.NO;
import static fi.riista.feature.common.entity.Required.VOLUNTARY;
import static fi.riista.feature.common.entity.Required.YES;

public class ObservationFieldRequirements {

    // Observation field names
    public static final String FIELD_OBSERVATION_CATEGORY = "observationCategory";
    public static final String FIELD_WITHIN_MOOSE_HUNTING = "withinMooseHunting";
    public static final String FIELD_WITHIN_DEER_HUNTING = "withinDeerHunting";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_MOOSELIKE_MALE_AMOUNT = "mooselikeMaleAmount";
    public static final String FIELD_MOOSELIKE_FEMALE_AMOUNT = "mooselikeFemaleAmount";
    public static final String FIELD_MOOSELIKE_CALF_AMOUNT = "mooselikeCalfAmount";
    public static final String FIELD_MOOSELIKE_FEMALE_1CALF_AMOUNT = "mooselikeFemale1CalfAmount";
    public static final String FIELD_MOOSELIKE_FEMALE_2CALFS_AMOUNT = "mooselikeFemale2CalfsAmount";
    public static final String FIELD_MOOSELIKE_FEMALE_3CALFS_AMOUNT = "mooselikeFemale3CalfsAmount";
    public static final String FIELD_MOOSELIKE_FEMALE_4CALFS_AMOUNT = "mooselikeFemale4CalfsAmount";
    public static final String FIELD_MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT = "mooselikeUnknownSpecimenAmount";
    public static final String FIELD_VERIFIED_BY_CARNIVORE_AUTHORITY = "verifiedByCarnivoreAuthority";
    public static final String FIELD_OBSERVER_NAME = "observerName";
    public static final String FIELD_OBSERVER_PHONE_NUMBER = "observerPhoneNumber";
    public static final String FIELD_OFFICIAL_ADDITIONAL_INFO = "officialAdditionalInfo";
    public static final String FIELD_DEER_HUNTING_TYPE = "deerHuntingType";
    public static final String FIELD_DEER_HUNTING_TYPE_DESCRIPTION = "deerHuntingTypeDescription";

    // Observation specimen field names
    public static final String FIELD_AGE = "age";
    public static final String FIELD_GENDER = "gender";
    public static final String FIELD_WIDTH_OF_PAW = "widthOfPaw";
    public static final String FIELD_LENGTH_OF_PAW = "lengthOfPaw";
    public static final String FIELD_STATE = "state";
    public static final String FIELD_MARKING = "marking";

    public static Map<String, Required> getStaticBaseFields(@Nonnull final ObservationContextSensitiveFields ctxFields) {

        return builderForStaticContextSensitiveBaseFields(ctxFields).build();
    }

    public static Map<String, Required> getStaticBaseFields(@Nonnull final ObservationBaseFields baseFields,
                                                            @Nonnull final ObservationContextSensitiveFields ctxFields) {

        Objects.requireNonNull(baseFields, "baseFields is null");

        return builderForStaticContextSensitiveBaseFields(ctxFields)
                .put(FIELD_OBSERVATION_CATEGORY, YES)
                .build();
    }

    private static ImmutableMap.Builder<String, Required> builderForStaticContextSensitiveBaseFields(
            @Nonnull final ObservationContextSensitiveFields ctxFields) {

        Objects.requireNonNull(ctxFields);

        return ImmutableMap.<String, Required> builder()
                .put(FIELD_MOOSELIKE_MALE_AMOUNT, ctxFields.getMooselikeMaleAmount())
                .put(FIELD_MOOSELIKE_FEMALE_AMOUNT, ctxFields.getMooselikeFemaleAmount())
                .put(FIELD_MOOSELIKE_CALF_AMOUNT, ctxFields.getMooselikeCalfAmount())
                .put(FIELD_MOOSELIKE_FEMALE_1CALF_AMOUNT, ctxFields.getMooselikeFemale1CalfAmount())
                .put(FIELD_MOOSELIKE_FEMALE_2CALFS_AMOUNT, ctxFields.getMooselikeFemale2CalfsAmount())
                .put(FIELD_MOOSELIKE_FEMALE_3CALFS_AMOUNT, ctxFields.getMooselikeFemale3CalfsAmount())
                .put(FIELD_MOOSELIKE_FEMALE_4CALFS_AMOUNT, ctxFields.getMooselikeFemale4CalfsAmount())
                .put(FIELD_MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT, ctxFields.getMooselikeUnknownSpecimenAmount());
    }

    public static Map<String, Required> getStaticSpecimenFields(
            @Nonnull final ObservationContextSensitiveFields ctxFields) {

        Objects.requireNonNull(ctxFields);

        final ImmutableMap.Builder<String, Required> builder = ImmutableMap.<String, Required> builder()
                .put(FIELD_AGE, ctxFields.getAge())
                .put(FIELD_GENDER, ctxFields.getGender());

        final Map<ObservedGameState, Required> validGameStates = ctxFields.getValidGameStateRequirements();

        builder.put(FIELD_STATE, validGameStates.isEmpty()
                ? NO
                : validGameStates.containsValue(YES) ? YES : VOLUNTARY);

        final Map<GameMarking, Required> validGameMarkings = ctxFields.getValidGameMarkingRequirements();

        builder.put(FIELD_MARKING, validGameMarkings.isEmpty()
                ? NO
                : validGameMarkings.containsValue(YES) ? YES : VOLUNTARY);

        return builder.build();
    }

    public static Map<String, DynamicObservationFieldPresence> getDynamicBaseFields(
           @Nonnull final ObservationContextSensitiveFields ctxFields) {

        Objects.requireNonNull(ctxFields);

        return ImmutableMap.<String, DynamicObservationFieldPresence> builder()
                .put(FIELD_AMOUNT, ctxFields.getAmount())
                .put(FIELD_VERIFIED_BY_CARNIVORE_AUTHORITY, ctxFields.getVerifiedByCarnivoreAuthority())
                .put(FIELD_OBSERVER_NAME, ctxFields.getObserverName())
                .put(FIELD_OBSERVER_PHONE_NUMBER, ctxFields.getObserverPhoneNumber())
                .put(FIELD_OFFICIAL_ADDITIONAL_INFO, ctxFields.getOfficialAdditionalInfo())
                .put(FIELD_DEER_HUNTING_TYPE, ctxFields.getDeerHuntingType())
                .put(FIELD_DEER_HUNTING_TYPE_DESCRIPTION, ctxFields.getDeerHuntingTypeDescription())
                .build();
    }

    public static Map<String, DynamicObservationFieldPresence> getDynamicSpecimenFields(
           @Nonnull final ObservationContextSensitiveFields ctxFields) {

        Objects.requireNonNull(ctxFields);

        return ImmutableMap.<String, DynamicObservationFieldPresence> builder()
                .put(FIELD_WIDTH_OF_PAW, ctxFields.getWidthOfPaw())
                .put(FIELD_LENGTH_OF_PAW, ctxFields.getLengthOfPaw())
                .build();
    }
}
