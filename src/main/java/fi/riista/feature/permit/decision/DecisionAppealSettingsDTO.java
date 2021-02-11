package fi.riista.feature.permit.decision;

import fi.riista.feature.common.decision.AppealStatus;

import javax.validation.constraints.NotNull;

public class DecisionAppealSettingsDTO {

    @NotNull
    private Long decisionId;

    private AppealStatus appealStatus;

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(final Long decisionId) {
        this.decisionId = decisionId;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }
}
