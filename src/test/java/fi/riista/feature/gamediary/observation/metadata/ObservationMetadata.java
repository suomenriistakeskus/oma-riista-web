package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationDTOBase;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import javax.annotation.Nonnull;
import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.GameSpecies.isLargeCarnivore;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.fromIntValue;
import static java.util.Objects.requireNonNull;

public class ObservationMetadata extends ObservationSpecimenOps implements ValueGeneratorMixin {

    private final ObservationBaseFields baseFields;
    private final ObservationContextSensitiveFields contextSensitiveFields;

    public ObservationMetadata(@Nonnull final ObservationBaseFields baseFields,
                               @Nonnull final ObservationContextSensitiveFields contextSensitiveFields) {

        super(baseFields.getSpecies().getOfficialCode(), fromIntValue(baseFields.getMetadataVersion()));

        this.baseFields = baseFields;
        this.contextSensitiveFields = requireNonNull(contextSensitiveFields, "contextSensitiveFields is null");

        checkArgument(baseFields.getSpecies().equals(contextSensitiveFields.getSpecies()), "Game species mismatch");

        checkArgument(
                baseFields.getMetadataVersion() == contextSensitiveFields.getMetadataVersion(),
                "Metadata version mismatch");

        checkArgument(
                baseFields.getWithinMooseHunting().isNonNullValueLegal() || !contextSensitiveFields.isWithinMooseHunting(),
                "Conflicting metadata with regard to associability to moose hunting");
    }

    public ObservationCategory getObservationCategory() {
        return contextSensitiveFields.getObservationCategory();
    }

    public boolean isAmountLegal(final boolean hasCarnivoreAuthority) {
        return contextSensitiveFields.getAmount().isCarnivoreFieldAllowed(hasCarnivoreAuthority);
    }

    public boolean isAmountRequired() {
        return isAmountLegal(false);
    }

    public void mutateDeerHuntingTypeFields(@Nonnull final ObservationDTOBase dto) {
        requireNonNull(dto);

        if (contextSensitiveFields.getDeerHuntingType().isDeerHuntingFieldAllowed()) {
            dto.setDeerHuntingType(someOtherThan(dto.getDeerHuntingType(), DeerHuntingType.class));
        } else {
            throw new IllegalStateException("Cannot mutate deer pilot field: deerHuntingType");
        }

        if (contextSensitiveFields.getDeerHuntingTypeDescription().isDeerHuntingFieldAllowed()) {
            dto.setDeerHuntingTypeDescription("deerHuntingTypeDescription-" + nextPositiveInt());
        } else {
            throw new IllegalStateException("Cannot mutate deer pilot field: deerHuntingTypeDescription");
        }
    }

    public void mutateMooselikeAmountFields(@Nonnull final ObservationDTOBase dto) {
        requireNonNull(dto);

        if (contextSensitiveFields.getMooselikeMaleAmount().isNonNullValueLegal()) {
            dto.setMooselikeMaleAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeMaleAmount(null);
        }

        if (contextSensitiveFields.getMooselikeFemaleAmount().isNonNullValueLegal()) {
            dto.setMooselikeFemaleAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeFemaleAmount(null);
        }

        if (contextSensitiveFields.getMooselikeFemale1CalfAmount().isNonNullValueLegal()) {
            dto.setMooselikeFemale1CalfAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeFemale1CalfAmount(null);
        }

        if (contextSensitiveFields.getMooselikeFemale2CalfsAmount().isNonNullValueLegal()) {
            dto.setMooselikeFemale2CalfsAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeFemale2CalfsAmount(null);
        }

        if (contextSensitiveFields.getMooselikeFemale3CalfsAmount().isNonNullValueLegal()) {
            dto.setMooselikeFemale3CalfsAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeFemale3CalfsAmount(null);
        }

        if (contextSensitiveFields.getMooselikeFemale4CalfsAmount().isNonNullValueLegal()) {
            dto.setMooselikeFemale4CalfsAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeFemale4CalfsAmount(null);
        }

        if (contextSensitiveFields.getMooselikeCalfAmount().isNonNullValueLegal()) {
            dto.setMooselikeCalfAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeCalfAmount(null);
        }

        if (contextSensitiveFields.getMooselikeUnknownSpecimenAmount().isNonNullValueLegal()) {
            dto.setMooselikeUnknownSpecimenAmount(nextPositiveIntAtMost(50));
        } else {
            dto.setMooselikeUnknownSpecimenAmount(null);
        }
    }

    public void withMooselikeAmountFieldsCleared(@Nonnull final ObservationDTOBase dto) {
        requireNonNull(dto);

        dto.setMooselikeMaleAmount(null);
        dto.setMooselikeFemaleAmount(null);
        dto.setMooselikeFemale1CalfAmount(null);
        dto.setMooselikeFemale2CalfsAmount(null);
        dto.setMooselikeFemale3CalfsAmount(null);
        dto.setMooselikeFemale4CalfsAmount(null);
        dto.setMooselikeCalfAmount(null);
        dto.setMooselikeUnknownSpecimenAmount(null);
    }

    public void mutateLargeCarnivoreFields(@Nonnull final ObservationDTOBase dto) {
        requireNonNull(dto);

        if (supportsLargeCarnivoreFields() && isLargeCarnivore(getGameSpeciesCode())) {
            dto.setInYardDistanceToResidence(nextPositiveIntAtMost(100));
        } else {
            dto.setInYardDistanceToResidence(null);
        }

        if (contextSensitiveFields.getVerifiedByCarnivoreAuthority().isCarnivoreFieldAllowed(/*hasCarnivoreAuthority*/true)) {
            dto.setVerifiedByCarnivoreAuthority(someOtherThan(dto.getVerifiedByCarnivoreAuthority()));
        } else {
            throw new IllegalStateException("Cannot mutate large carnivore field: verifiedByCarnivoreAuthority");
        }

        if (contextSensitiveFields.getObserverName().isCarnivoreFieldAllowed(/*hasCarnivoreAuthority*/true)) {
            dto.setObserverName("observerName-" + nextPositiveInt());
        } else {
            throw new IllegalStateException("Cannot mutate large carnivore field: observerName");
        }

        if (contextSensitiveFields.getObserverPhoneNumber().isCarnivoreFieldAllowed(/*hasCarnivoreAuthority*/true)) {
            dto.setObserverPhoneNumber(phoneNumber());
        } else {
            throw new IllegalStateException("Cannot mutate large carnivore field: observerPhoneNumber");
        }

        if (contextSensitiveFields.getOfficialAdditionalInfo().isCarnivoreFieldAllowed(/*hasCarnivoreAuthority*/true)) {
            dto.setOfficialAdditionalInfo("officialAdditionalInfo-" + nextPositiveInt());
        } else {
            throw new IllegalStateException("Cannot mutate large carnivore field: officialAdditionalInfo");
        }
    }

    public ObservationSpecimenDTO newObservationSpecimenDTO(final boolean hasCarnivoreAuthority) {
        final ObservationSpecimenDTO dto = new ObservationSpecimenDTO();
        mutateContent(dto, hasCarnivoreAuthority);
        return dto;
    }

    /**
     * Mutates content of given ObservationSpecimenDTO object. ID and revision
     * fields are left intact.
     */
    public void mutateContent(@Nonnull final ObservationSpecimenDTO dto, final boolean hasCarnivoreAuthority) {
        requireNonNull(dto);

        if (contextSensitiveFields.getGender().isNonNullValueLegal()) {
            dto.setGender(someOtherThan(dto.getGender(), GameGender.class));
        } else {
            dto.setGender(null);
        }

        if (contextSensitiveFields.getAge().isNonNullValueLegal()) {
            dto.setAge(someOtherThan(dto.getAge(), contextSensitiveFields.getAllowedGameAges()));
        } else {
            dto.setAge(null);
        }

        final EnumSet<ObservedGameState> validStates = contextSensitiveFields.getAllowedGameStates();

        if (dto.getState() == null && !validStates.isEmpty()) {
            dto.setState(some(validStates));
        } else if (validStates.size() > 1) {
            dto.setState(someOtherThan(dto.getState(), validStates));
        } else {
            dto.setState(null);
        }

        final EnumSet<GameMarking> validMarkings = contextSensitiveFields.getAllowedGameMarkings();

        if (dto.getMarking() == null && !validMarkings.isEmpty()) {
            dto.setMarking(some(validMarkings));
        } else if (validMarkings.size() > 1) {
            dto.setMarking(someOtherThan(dto.getMarking(), validMarkings));
        } else {
            dto.setMarking(null);
        }

        mutateLargeCarnivoreFields(dto, hasCarnivoreAuthority);
    }

    private void mutateLargeCarnivoreFields(@Nonnull final ObservationSpecimenDTO dto,
                                            final boolean hasCarnivoreAuthority) {

        if (contextSensitiveFields.getWidthOfPaw().isCarnivoreFieldAllowed(hasCarnivoreAuthority)) {
            final Double currentW = dto.getWidthOfPaw();
            final double replacingW =
                    currentW == null || currentW + 1.0 > ObservationSpecimenOps.MAX_PAW_WIDTH_OF_OTHER_LARGE_CARNIVORES
                            ? ObservationSpecimenOps.MIN_PAW_WIDTH_OF_LARGE_CARNIVORES
                            : currentW + 1.0;
            dto.setWidthOfPaw(replacingW);
        } else {
            dto.setWidthOfPaw(null);
        }

        if (contextSensitiveFields.getLengthOfPaw().isCarnivoreFieldAllowed(hasCarnivoreAuthority)) {
            final Double currentL = dto.getLengthOfPaw();
            final double replacingL =
                    currentL == null || currentL + 1.0 > ObservationSpecimenOps.MAX_PAW_LENGTH_OF_LARGE_CARNIVORES
                            ? ObservationSpecimenOps.MIN_PAW_LENGTH_OF_LARGE_CARNIVORES
                            : currentL + 1.0;
            dto.setLengthOfPaw(replacingL);
        } else {
            dto.setLengthOfPaw(null);
        }
    }

    public Tuple2<Double, Double> generateWidthAndLengthOfPaw(final boolean hasCarnivoreAuthority) {
        Double widthOfPaw = null;

        if (contextSensitiveFields.getWidthOfPaw().isCarnivoreFieldAllowed(hasCarnivoreAuthority)) {
            final int iMinW = Double.valueOf(MIN_PAW_WIDTH_OF_LARGE_CARNIVORES).intValue();
            final int iMaxW = Double.valueOf(MAX_PAW_WIDTH_OF_OTHER_LARGE_CARNIVORES).intValue();

            widthOfPaw = Integer.valueOf(nextIntBetween(iMinW, iMaxW)).doubleValue();
        }

        Double lengthOfPaw = null;

        if (contextSensitiveFields.getLengthOfPaw().isCarnivoreFieldAllowed(hasCarnivoreAuthority)) {
            final int iMinL = Double.valueOf(MIN_PAW_LENGTH_OF_LARGE_CARNIVORES).intValue();
            final int iMaxL = Double.valueOf(MAX_PAW_LENGTH_OF_LARGE_CARNIVORES).intValue();

            lengthOfPaw = Integer.valueOf(nextIntBetween(iMinL, iMaxL)).doubleValue();
        }

        return Tuple.of(widthOfPaw, lengthOfPaw);
    }

    // Accessors -->

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    public ObservationBaseFields getBaseFields() {
        return baseFields;
    }

    public ObservationContextSensitiveFields getContextSensitiveFields() {
        return contextSensitiveFields;
    }

    // Deep accessors -->

    public GameSpecies getSpecies() {
        return baseFields.getSpecies();
    }

    public ObservationType getObservationType() {
        return contextSensitiveFields.getObservationType();
    }
}
