package fi.riista.feature.permit.decision.derogation;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class PermitDecisionDerogationReasonsDTO {

    public static PermitDecisionDerogationReasonsDTO of(@Nonnull List<PermitDecisionDerogationReasonDTO> list) {
        final PermitDecisionDerogationReasonsDTO dto = new PermitDecisionDerogationReasonsDTO();
        dto.setReasons(list);
        return dto;
    }

    @Valid
    @NotNull
    private List<PermitDecisionDerogationReasonDTO> reasons;

    public List<PermitDecisionDerogationReasonDTO> getReasons() {
        return reasons;
    }

    public void setReasons(List<PermitDecisionDerogationReasonDTO> reasons) {
        this.reasons = reasons;
    }

    @AssertTrue
    public boolean isDistinctList() {
        requireNonNull(reasons);

        final long distinctCount = reasons.stream().map(PermitDecisionDerogationReasonDTO::getReasonType).distinct().count();

         return distinctCount == reasons.size();
    }
}
