package fi.riista.feature.permit.application.derogation.reasons;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DerogationPermitApplicationReasonsDTO {

    public static DerogationPermitApplicationReasonsDTO of(@Nonnull List<DerogationPermitApplicationLawSectionReasonsDTO> list) {
        final DerogationPermitApplicationReasonsDTO dto = new DerogationPermitApplicationReasonsDTO();

        dto.setReasons(requireNonNull(list));
        return dto;
    }

    @Valid
    @NotNull
    private List<DerogationPermitApplicationLawSectionReasonsDTO> reasons;


    public List<DerogationPermitApplicationLawSectionReasonsDTO> getReasons() {
        return reasons;
    }

    public void setReasons(List<DerogationPermitApplicationLawSectionReasonsDTO> reasons) {
        this.reasons = reasons;
    }


}
