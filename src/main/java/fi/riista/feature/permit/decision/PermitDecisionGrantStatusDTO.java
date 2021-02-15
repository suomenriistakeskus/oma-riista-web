package fi.riista.feature.permit.decision;

import fi.riista.feature.common.decision.GrantStatus;

import javax.validation.constraints.NotNull;

public class PermitDecisionGrantStatusDTO {

    @NotNull
    private GrantStatus grantStatus;

    public GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final GrantStatus grantStatus) {
        this.grantStatus = grantStatus;
    }
}
