package fi.riista.feature.common.decision.nomination.reference;

import javax.validation.constraints.NotNull;

public class UpdateNominationDecisionReferenceDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long referenceId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(final Long referenceId) {
        this.referenceId = referenceId;
    }
}
