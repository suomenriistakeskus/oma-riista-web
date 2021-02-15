package fi.riista.feature.permit.application.derogation.reasons;

import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonDTO;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DerogationPermitApplicationLawSectionReasonsDTO {

    public static DerogationPermitApplicationLawSectionReasonsDTO of(@Nonnull final DerogationLawSection lawSection,
                                                                     @Nonnull final String speciesCodes,
                                                                     @Nonnull final List<PermitDecisionDerogationReasonDTO> list) {
        final DerogationPermitApplicationLawSectionReasonsDTO dto =
                new DerogationPermitApplicationLawSectionReasonsDTO();
        dto.setLawSection(requireNonNull(lawSection));
        dto.setSpeciesCodes(requireNonNull(speciesCodes));
        dto.setLawSectionReasons(requireNonNull(list));
        return dto;
    }

    @NotNull
    private DerogationLawSection lawSection;

    @Valid
    @NotNull
    private List<PermitDecisionDerogationReasonDTO> lawSectionReasons;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotNull
    private String speciesCodes;

    public DerogationLawSection getLawSection() {
        return lawSection;
    }

    public void setLawSection(final DerogationLawSection lawSection) {
        this.lawSection = lawSection;
    }

    public List<PermitDecisionDerogationReasonDTO> getLawSectionReasons() {
        return lawSectionReasons;
    }

    public void setLawSectionReasons(final List<PermitDecisionDerogationReasonDTO> reasons) {
        this.lawSectionReasons = reasons;
    }

    public String getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(final String speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @AssertTrue
    public boolean isDistinctList() {
        requireNonNull(lawSectionReasons);

        final long distinctCount =
                lawSectionReasons.stream().map(PermitDecisionDerogationReasonDTO::getReasonType).distinct().count();

        return distinctCount == lawSectionReasons.size();
    }


}
