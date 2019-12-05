package fi.riista.feature.permit.decision.derogation;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class PermitDecisionDerogationReasonsDTO {

    public static PermitDecisionDerogationReasonsDTO of(final @Nonnull List<PermitDecisionDerogationLawSectionDTO> list) {
        final PermitDecisionDerogationReasonsDTO dto = new PermitDecisionDerogationReasonsDTO();
        dto.setLawSections(list);
        return dto;
    }

    @Valid
    @NotNull
    private List<PermitDecisionDerogationLawSectionDTO> lawSections;

    public List<PermitDecisionDerogationLawSectionDTO> getLawSections() {
        return lawSections;
    }

    public void setLawSections(final List<PermitDecisionDerogationLawSectionDTO> lawSections) {
        this.lawSections = lawSections;
    }
}
