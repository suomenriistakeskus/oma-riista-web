package fi.riista.feature.permit.decision.derogation;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class PermitDecisionDerogationLawSectionDTO {

    public static PermitDecisionDerogationLawSectionDTO of(final @Nonnull DerogationLawSection lawSection,
                                                           final @Nonnull List<PermitDecisionDerogationReasonDTO> list) {
        final PermitDecisionDerogationLawSectionDTO dto = new PermitDecisionDerogationLawSectionDTO();
        dto.setLawSection(lawSection);
        dto.setReasons(list);
        return dto;
    }

    @NotNull
    private DerogationLawSection lawSection;

    @Valid
    @NotNull
    private List<PermitDecisionDerogationReasonDTO> reasons;

    public DerogationLawSection getLawSection() {
        return lawSection;
    }

    public void setLawSection(final DerogationLawSection lawSection) {
        this.lawSection = lawSection;
    }

    public List<PermitDecisionDerogationReasonDTO> getReasons() {
        return reasons;
    }

    public void setReasons(final List<PermitDecisionDerogationReasonDTO> reasons) {
        this.reasons = reasons;
    }

    @AssertTrue
    public boolean isDistinctList() {
        requireNonNull(reasons);

        final long distinctCount =
                reasons.stream().map(PermitDecisionDerogationReasonDTO::getReasonType).distinct().count();

        return distinctCount == reasons.size();
    }
}
