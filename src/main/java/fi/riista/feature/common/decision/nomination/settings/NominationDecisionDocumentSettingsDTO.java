package fi.riista.feature.common.decision.nomination.settings;

import javax.validation.constraints.NotNull;
import java.util.Locale;

public class NominationDecisionDocumentSettingsDTO {

    @NotNull
    private Long decisionId;

    @NotNull
    private Locale locale;

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(final Long decisionId) {
        this.decisionId = decisionId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

}
