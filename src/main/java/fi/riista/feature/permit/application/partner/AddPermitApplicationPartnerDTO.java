package fi.riista.feature.permit.application.partner;

import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AddPermitApplicationPartnerDTO {
    @NotNull
    private Long applicationId;

    @NotBlank
    @Size(min = 5)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String externalId;

    public Long getApplicationId() {
        return applicationId;
    }

    public String getExternalId() {
        return externalId;
    }
}
