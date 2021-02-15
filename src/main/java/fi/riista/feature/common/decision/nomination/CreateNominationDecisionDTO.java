package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.organization.occupation.OccupationType;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Locale;

public class CreateNominationDecisionDTO {

    @Size(min = 3, max = 3)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyCode;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private NominationDecision.NominationDecisionType nominationDecisionType;

    @NotNull
    private Locale locale;

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public NominationDecision.NominationDecisionType getNominationDecisionType() {
        return nominationDecisionType;
    }

    public void setNominationDecisionType(final NominationDecision.NominationDecisionType nominationDecisionType) {
        this.nominationDecisionType = nominationDecisionType;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }
}
