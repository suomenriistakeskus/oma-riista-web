package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.permit.application.bird.ProtectedAreaType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PermitDecisionProtectedAreaTypeDTO {

    public static List<PermitDecisionProtectedAreaTypeDTO> toDTOs(Collection<ProtectedAreaType> iterable) {

        // Map all enumeration values, set checked flag based on entity collection
        return Arrays.stream(ProtectedAreaType.values())
                .map(val -> new PermitDecisionProtectedAreaTypeDTO(val, iterable.contains(val)))
                .collect(toList());
    }

    private ProtectedAreaType areaType;
    private boolean checked;

    public ProtectedAreaType getAreaType() {
        return areaType;
    }

    public boolean isChecked() {
        return checked;
    }

    public PermitDecisionProtectedAreaTypeDTO() {

    }

    public PermitDecisionProtectedAreaTypeDTO(ProtectedAreaType type, boolean checked) {
        this.areaType = type;
        this.checked = checked;
    }

}
