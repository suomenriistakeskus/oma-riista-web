package fi.riista.feature.common.decision.nomination.delivery;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotBlank;

public class NominationDecisionDeliveryDTO {

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String email;

    public NominationDecisionDeliveryDTO(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public NominationDecisionDeliveryDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
