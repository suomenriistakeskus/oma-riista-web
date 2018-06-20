package fi.riista.feature.permit.decision;

import javax.validation.constraints.NotNull;

public class CreatePermitDecisionDTO {
    @NotNull
    private Long applicationId;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final Long applicationId) {
        this.applicationId = applicationId;
    }
}
