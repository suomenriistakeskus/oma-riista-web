package fi.riista.feature.permit.decision.derogation;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class PermitDecisionDerogationReasonDTO {

    public static List<PermitDecisionDerogationReasonDTO> toDTOs(final Collection<PermitDecisionDerogationReasonType> iterable, final DerogationLawSection section) {

        // Map all enumeration values, set checked flag based on entity collection
        return Arrays.stream(PermitDecisionDerogationReasonType.values())
                .filter(val -> section == val.getLawSection())
                .filter(val -> val != REASON_POPULATION_PRESERVATION)
                .map(val -> new PermitDecisionDerogationReasonDTO(val, iterable.contains(val)))
                .collect(toList());
    }

    public static List<PermitDecisionDerogationReasonDTO> toDTOsForPopulationPreservation(final Collection<PermitDecisionDerogationReasonType> iterable) {

        checkArgument(iterable.size() <= 1);

        if (iterable.isEmpty()) {
            return ImmutableList.of(new PermitDecisionDerogationReasonDTO(REASON_POPULATION_PRESERVATION, false));
        } else {
            checkArgument(iterable.contains(REASON_POPULATION_PRESERVATION));
            return ImmutableList.of(new PermitDecisionDerogationReasonDTO(REASON_POPULATION_PRESERVATION, true));
        }

    }

    private PermitDecisionDerogationReasonType reasonType;
    private boolean checked;

    public PermitDecisionDerogationReasonType getReasonType() {
        return reasonType;
    }

    public boolean isChecked() {
        return checked;
    }

    public PermitDecisionDerogationReasonDTO() {

    }

    public PermitDecisionDerogationReasonDTO(PermitDecisionDerogationReasonType type, boolean checked) {
        this.reasonType = requireNonNull(type);
        this.checked = checked;
    }

}
