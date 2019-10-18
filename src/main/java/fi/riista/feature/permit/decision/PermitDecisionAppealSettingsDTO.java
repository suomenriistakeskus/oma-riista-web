package fi.riista.feature.permit.decision;

import javax.validation.constraints.NotNull;

public class PermitDecisionAppealSettingsDTO {

    @NotNull
    private Long decisionId;

    private PermitDecision.AppealStatus appealStatus;

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(final Long decisionId) {
        this.decisionId = decisionId;
    }

    public PermitDecision.AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final PermitDecision.AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }
}
