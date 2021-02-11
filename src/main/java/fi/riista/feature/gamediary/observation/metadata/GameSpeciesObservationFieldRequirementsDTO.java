package fi.riista.feature.gamediary.observation.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.common.entity.RequiredWithinDeerPilot;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class GameSpeciesObservationFieldRequirementsDTO {

    public static class ContextSensitiveFieldSetDTO implements Comparable<ContextSensitiveFieldSetDTO> {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final Boolean withinMooseHunting;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final ObservationCategory category;

        private final ObservationType type;

        private final Map<String, Enum<?>> baseFields;
        private final Map<String, Enum<?>> specimenFields;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final EnumSet<ObservedGameAge> allowedAges;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final EnumSet<ObservedGameState> allowedStates;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final EnumSet<GameMarking> allowedMarkings;

        public ContextSensitiveFieldSetDTO(@Nonnull final ObservationContextSensitiveFields ctxFields,
                                           final boolean omitNullValueRequirements) {

            this.type = ctxFields.getObservationType();

            if (ObservationSpecVersion.fromIntValue(ctxFields.getMetadataVersion()).supportsCategory()) {
                // Within deer hunting observations are supported => use category
                this.withinMooseHunting = null;
                this.category = ctxFields.getObservationCategory();
            } else {
                // Within deer hunting observations are *not* supported => use withinMooseHunting
                this.withinMooseHunting = ctxFields.isWithinMooseHunting();
                this.category = null;
            }

            final Predicate<Required> staticTest = required -> !omitNullValueRequirements || required != Required.NO;

            final Predicate<DynamicObservationFieldPresence> dynamicTest =
                    required -> !omitNullValueRequirements || required != DynamicObservationFieldPresence.NO;

            final Map<String, Required> staticBase =
                    Maps.filterValues(ObservationFieldRequirements.getStaticBaseFields(ctxFields), staticTest);

            final Map<String, DynamicObservationFieldPresence> dynamicBase =
                    Maps.filterValues(ObservationFieldRequirements.getDynamicBaseFields(ctxFields), dynamicTest);

            this.baseFields = ImmutableMap.<String, Enum<?>> builder()
                    .putAll(staticBase)
                    .putAll(dynamicBase)
                    .build();

            final Map<String, Required> staticSpecimen =
                    Maps.filterValues(ObservationFieldRequirements.getStaticSpecimenFields(ctxFields), staticTest);

            final Map<String, DynamicObservationFieldPresence> dynamicSpecimen =
                    Maps.filterValues(ObservationFieldRequirements.getDynamicSpecimenFields(ctxFields), dynamicTest);

            this.specimenFields = ImmutableMap.<String, Enum<?>> builder()
                    .putAll(staticSpecimen)
                    .putAll(dynamicSpecimen)
                    .build();

            this.allowedAges = ctxFields.getAllowedGameAges();
            this.allowedStates = ctxFields.getAllowedGameStates();
            this.allowedMarkings = ctxFields.getAllowedGameMarkings();
        }

        @Override
        public int compareTo(final ContextSensitiveFieldSetDTO that) {
            return withinMooseHunting != that.withinMooseHunting
                    ? withinMooseHunting ? -1 : 1
                    : type.ordinal() - that.type.ordinal();
        }

        // Accessors/mutators -->

        public Boolean isWithinMooseHunting() {
            return withinMooseHunting;
        }

        public ObservationCategory getCategory() {
            return category;
        }

        public ObservationType getType() {
            return type;
        }

        public Map<String, Enum<?>> getBaseFields() {
            return baseFields;
        }

        public Map<String, Enum<?>> getSpecimenFields() {
            return specimenFields;
        }

        public Enum<?> getState() {
            return getSpecimenFields().get(ObservationFieldRequirements.FIELD_STATE);
        }

        public Enum<?> getMarking() {
            return getSpecimenFields().get(ObservationFieldRequirements.FIELD_MARKING);
        }

        public EnumSet<ObservedGameAge> getAllowedAges() {
            return allowedAges;
        }

        public EnumSet<ObservedGameState> getAllowedStates() {
            return allowedStates;
        }

        public EnumSet<GameMarking> getAllowedMarkings() {
            return allowedMarkings;
        }
    }

    private final int gameSpeciesCode;

    private final Map<String, RequiredWithinDeerPilot> baseFields;
    private final Map<String, Required> specimenFields = Collections.emptyMap(); // currently empty

    private final List<ContextSensitiveFieldSetDTO> contextSensitiveFieldSets;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Double minWidthOfPaw;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Double maxWidthOfPaw;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Double minLengthOfPaw;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Double maxLengthOfPaw;

    public GameSpeciesObservationFieldRequirementsDTO(@Nonnull final ObservationBaseFields baseFields,
                                                      @Nonnull final Collection<ObservationContextSensitiveFields> ctxFieldsets,
                                                      final boolean omitNullValueRequirements) {

        Objects.requireNonNull(baseFields, "baseFields is null");
        Objects.requireNonNull(ctxFieldsets, "ctxFieldsets is null");

        this.gameSpeciesCode = Objects.requireNonNull(baseFields.getSpecies(), "species is null").getOfficialCode();

        final Required withinMooseHuntingReq = baseFields.getWithinMooseHunting();
        final RequiredWithinDeerPilot withinDeerHuntingReq = baseFields.getWithinDeerHunting();
        final ImmutableMap.Builder<String, RequiredWithinDeerPilot> baseFieldBuilder = ImmutableMap.builder();

        if (!omitNullValueRequirements || withinMooseHuntingReq != Required.NO) {
            baseFieldBuilder.put(
                    ObservationFieldRequirements.FIELD_WITHIN_MOOSE_HUNTING,
                    RequiredWithinDeerPilot.from(withinMooseHuntingReq));
        }

        if (!omitNullValueRequirements || withinDeerHuntingReq != RequiredWithinDeerPilot.NO) {
            baseFieldBuilder.put(ObservationFieldRequirements.FIELD_WITHIN_DEER_HUNTING, withinDeerHuntingReq);
        }

        this.baseFields = baseFieldBuilder.build();

        this.contextSensitiveFieldSets = ctxFieldsets.stream()
                .map(ctxFields -> new ContextSensitiveFieldSetDTO(ctxFields, omitNullValueRequirements))
                .sorted()
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        if (ctxFieldsets.stream().anyMatch(ctx -> ctx.getWidthOfPaw().isPresentInAnyContext())) {
            this.minWidthOfPaw = ObservationSpecimenOps.getMinWidthOfPaw(gameSpeciesCode);
            this.maxWidthOfPaw = ObservationSpecimenOps.getMaxWidthOfPaw(gameSpeciesCode);
        } else {
            this.minWidthOfPaw = null;
            this.maxWidthOfPaw = null;
        }

        if (ctxFieldsets.stream().anyMatch(ctx -> ctx.getLengthOfPaw().isPresentInAnyContext())) {
            this.minLengthOfPaw = ObservationSpecimenOps.getMinLengthOfPaw(gameSpeciesCode);
            this.maxLengthOfPaw = ObservationSpecimenOps.getMaxLengthOfPaw(gameSpeciesCode);
        } else {
            this.minLengthOfPaw = null;
            this.maxLengthOfPaw = null;
        }
    }

    // Accessors/mutators -->

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public Map<String, RequiredWithinDeerPilot> getBaseFields() {
        return baseFields;
    }

    public Map<String, Required> getSpecimenFields() {
        return specimenFields;
    }

    public List<ContextSensitiveFieldSetDTO> getContextSensitiveFieldSets() {
        return contextSensitiveFieldSets;
    }

    public Double getMinWidthOfPaw() {
        return minWidthOfPaw;
    }

    public Double getMaxWidthOfPaw() {
        return maxWidthOfPaw;
    }

    public Double getMinLengthOfPaw() {
        return minLengthOfPaw;
    }

    public Double getMaxLengthOfPaw() {
        return maxLengthOfPaw;
    }
}
