package fi.riista.feature.permit.decision.delivery.rkarecipient;

import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;

public class PermitDecisionRkaRecipientDTO {

    private Long id;
    private Long rkaId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Email
    private String email;

    public PermitDecisionRkaRecipientDTO(final Long id,
                                         final Long rkaId,
                                         final LocalisedString name,
                                         final String email) {
        this.id = id;
        this.rkaId = rkaId;
        this.nameFI = name.getFinnish();
        this.nameSV = name.getSwedish();
        this.email = email;
    }

    public PermitDecisionRkaRecipientDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getRkaId() {
        return rkaId;
    }

    public void setRkaId(final Long rkaId) {
        this.rkaId = rkaId;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(final String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(final String nameSV) {
        this.nameSV = nameSV;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
