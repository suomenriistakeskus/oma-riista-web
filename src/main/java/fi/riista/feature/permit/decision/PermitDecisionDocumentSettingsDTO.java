package fi.riista.feature.permit.decision;

import javax.validation.constraints.NotNull;
import java.util.Locale;

public class PermitDecisionDocumentSettingsDTO {

    @NotNull
    private Long decisionId;

    @NotNull
    private PermitDecision.DecisionType decisionType;

    @NotNull
    private Locale locale;

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(final Long decisionId) {
        this.decisionId = decisionId;
    }

    public PermitDecision.DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final PermitDecision.DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }
}
