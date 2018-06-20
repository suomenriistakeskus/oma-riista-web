package fi.riista.feature.permit.decision;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class PermitDecisionUnlockDTO {
    @NotNull
    private Long id;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String unlockReason;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUnlockReason() {
        return unlockReason;
    }

    public void setUnlockReason(final String unlockReason) {
        this.unlockReason = unlockReason;
    }
}