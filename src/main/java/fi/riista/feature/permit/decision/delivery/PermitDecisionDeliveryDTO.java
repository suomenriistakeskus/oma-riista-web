package fi.riista.feature.permit.decision.delivery;

import org.hibernate.validator.constraints.SafeHtml;

public class PermitDecisionDeliveryDTO {

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String email;

    public PermitDecisionDeliveryDTO(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public PermitDecisionDeliveryDTO() {
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
