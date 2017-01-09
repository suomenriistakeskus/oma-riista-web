package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;

import java.util.EnumSet;
import java.util.Objects;

public interface CanPopulateObservationSpecimen extends ValueGeneratorMixin {

    ObservationContextSensitiveFields getContextSensitiveFields();

    default ObservationSpecimenDTO newObservationSpecimenDTO() {
        final ObservationSpecimenDTO dto = new ObservationSpecimenDTO();
        mutateContent(dto);
        return dto;
    }

    /**
     * Mutates content of given ObservationSpecimenDTO object. ID and revision
     * fields are left intact.
     */
    default void mutateContent(@Nonnull final ObservationSpecimenDTO dto) {
        Objects.requireNonNull(dto);
        final ObservationContextSensitiveFields ctxFields = getContextSensitiveFields();

        if (ctxFields.getGender().isAllowedField()) {
            dto.setGender(someOtherThan(dto.getGender(), GameGender.class));
        }

        if (ctxFields.getAge().isAllowedField()) {
            dto.setAge(someOtherThan(dto.getAge(), ctxFields.getAllowedGameAges()));
        }

        final EnumSet<ObservedGameState> validStates = ctxFields.getAllowedGameStates();

        if (dto.getState() == null && !validStates.isEmpty()) {
            dto.setState(some(validStates));
        } else if (validStates.size() > 1) {
            dto.setState(someOtherThan(dto.getState(), validStates));
        }

        final EnumSet<GameMarking> validMarkings = ctxFields.getAllowedGameMarkings();

        if (dto.getMarking() == null && !validMarkings.isEmpty()) {
            dto.setMarking(some(validMarkings));
        } else if (validMarkings.size() > 1) {
            dto.setMarking(someOtherThan(dto.getMarking(), validMarkings));
        }
    }

}
