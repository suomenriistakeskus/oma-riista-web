package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.permit.application.bird.ProtectedAreaType;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class PermitDecisionProtectedAreaTypesDTO {

    public static PermitDecisionProtectedAreaTypesDTO of(@Nonnull List<PermitDecisionProtectedAreaTypeDTO> list) {
        final PermitDecisionProtectedAreaTypesDTO dto = new PermitDecisionProtectedAreaTypesDTO();
        dto.setTypes(list);
        return dto;
    }

    @Valid
    @NotNull
    private List<PermitDecisionProtectedAreaTypeDTO> types;

    public List<PermitDecisionProtectedAreaTypeDTO> getTypes() {
        return types;
    }

    public void setTypes(List<PermitDecisionProtectedAreaTypeDTO> types) {
        this.types = types;
    }

    @AssertTrue
    public boolean isDistinctList() {
        requireNonNull(types);
        final long distinctCount = types.stream()
                .map(PermitDecisionProtectedAreaTypeDTO::getAreaType)
                .distinct()
                .count();

        return distinctCount == types.size();
    }

    @AssertTrue
    public boolean isCompleteList() {
        requireNonNull(types);
        return ProtectedAreaType.values().length == types.size();
    }
}
