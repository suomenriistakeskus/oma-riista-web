package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
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
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.GameSpecies.isLargeCarnivore;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.fromIntValue;

public class ObservationMetadata extends ObservationSpecimenOps implements ValueGeneratorMixin {

    private final ObservationBaseFields baseFields;
    private final ObservationContextSensitiveFields contextSensitiveFields;

    public ObservationMetadata(@Nonnull final ObservationBaseFields baseFields,
                               @Nonnull final ObservationContextSensitiveFields contextSensitiveFields) {

        super(baseFields.getSpecies().getOfficialCode(), fromIntValue(baseFields.getMetadataVersion()));

        this.baseFields = baseFields;
        this.contextSensitiveFields = Objects.requireNonNull(contextSensitiveFields, "contextSensitiveFields is null");

        checkArgument(baseFields.getSpecies().equals(contextSensitiveFields.getSpecies()), "Game species mismatch");

        checkArgument(
                baseFields.getMetadataVersion() == contextSensitiveFields.getMetadataVersion(),
                "Metadata version mismatch");

        checkArgument(
                baseFields.getWithinMooseHunting().isNonNullValueLegal() || !contextSensitiveFields.isWithinMooseHunting(),
                "Conflicting metadata with regard to associability to moose hunting");
    }

    public Boolean getWithinMooseHunting() {
        return baseFields.getWithinMooseHunting() != Required.NO ? contextSensitiveFields.isWithinMooseHunting() : null;
    }

    public boolean isAmountLegal(final boolean carnivoreAuthority) {
        return contextSensitiveFields.getAmount().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal();
    }

    public boolean isAmountRequired() {
        return contextSensitiveFields.getAmount().nonNullValueRequired();
    }

    public ObservationMetadata withAmount(@Nonnull final DynamicObservationFieldPresence amount) {
        Objects.requireNonNull(amount);
        contextSensitiveFields.setAmount(amount);
        return this;
    }

    public ObservationSpecimenDTO newObservationSpecimenDTO(final boolean carnivoreAuthority) {
        final ObservationSpecimenDTO dto = new ObservationSpecimenDTO();
        mutateContent(dto, carnivoreAuthority);
        return dto;
    }

    public void mutateMooselikeAmountFields(@Nonnull final ObservationDTOBase dto) {
        Objects.requireNonNull(dto);

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

    public void mutateLargeCarnivoreFields(@Nonnull final ObservationDTOBase dto, final boolean carnivoreAuthority) {
        Objects.requireNonNull(dto);

        if (supportsLargeCarnivoreFields() && isLargeCarnivore(getGameSpeciesCode())) {
            dto.setInYardDistanceToResidence(nextPositiveIntAtMost(100));
        } else {
            dto.setInYardDistanceToResidence(null);
        }

        if (contextSensitiveFields.getVerifiedByCarnivoreAuthority().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            dto.setVerifiedByCarnivoreAuthority(someOtherThan(dto.getVerifiedByCarnivoreAuthority()));
        } else {
            dto.setVerifiedByCarnivoreAuthority(null);
        }

        if (contextSensitiveFields.getObserverName().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            dto.setObserverName("observerName-" + nextPositiveInt());
        } else {
            dto.setObserverName(null);
        }

        if (contextSensitiveFields.getObserverPhoneNumber().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            dto.setObserverPhoneNumber(phoneNumber());
        } else {
            dto.setObserverPhoneNumber(null);
        }

        if (contextSensitiveFields.getOfficialAdditionalInfo().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            dto.setOfficialAdditionalInfo("officialAdditionalInfo-" + nextPositiveInt());
        } else {
            dto.setOfficialAdditionalInfo(null);
        }
    }

    /**
     * Mutates content of given ObservationSpecimenDTO object. ID and revision
     * fields are left intact.
     */
    public void mutateContent(@Nonnull final ObservationSpecimenDTO dto, final boolean carnivoreAuthority) {
        Objects.requireNonNull(dto);

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

        mutateLargeCarnivoreFields(dto, carnivoreAuthority);
    }

    private void mutateLargeCarnivoreFields(@Nonnull final ObservationSpecimenDTO dto,
                                            final boolean carnivoreAuthority) {

        if (contextSensitiveFields.getWidthOfPaw().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            final Double currentW = dto.getWidthOfPaw();
            final double replacingW =
                    currentW == null || currentW + 1.0 > ObservationSpecimenOps.MAX_PAW_WIDTH_OF_OTHER_LARGE_CARNIVORES
                            ? ObservationSpecimenOps.MIN_PAW_WIDTH_OF_LARGE_CARNIVORES
                            : currentW + 1.0;
            dto.setWidthOfPaw(replacingW);
        } else {
            dto.setWidthOfPaw(null);
        }

        if (contextSensitiveFields.getLengthOfPaw().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
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

    public Tuple2<Double, Double> generateWidthAndLengthOfPaw(final boolean carnivoreAuthority) {
        Double widthOfPaw = null;

        if (contextSensitiveFields.getWidthOfPaw().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            final int iMinW = Double.valueOf(MIN_PAW_WIDTH_OF_LARGE_CARNIVORES).intValue();
            final int iMaxW = Double.valueOf(MAX_PAW_WIDTH_OF_OTHER_LARGE_CARNIVORES).intValue();

            widthOfPaw = Integer.valueOf(nextIntBetween(iMinW, iMaxW)).doubleValue();
        }

        Double lengthOfPaw = null;

        if (contextSensitiveFields.getLengthOfPaw().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
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

    public DynamicObservationFieldPresence getAmount() {
        return contextSensitiveFields.getAmount();
    }
}
