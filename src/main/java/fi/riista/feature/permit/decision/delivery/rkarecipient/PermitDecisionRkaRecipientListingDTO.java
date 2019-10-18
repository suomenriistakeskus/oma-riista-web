package fi.riista.feature.permit.decision.delivery.rkarecipient;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;

public class PermitDecisionRkaRecipientListingDTO {

    private OrganisationNameDTO rka;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Email
    private String email;

    public PermitDecisionRkaRecipientListingDTO(final PermitDecisionRkaRecipient recipient,
                                                final OrganisationNameDTO rka) {

        this.rka = rka;
        final LocalisedString name = recipient.getNameLocalisation();
        this.nameFI = name.getFinnish();
        this.nameSV = name.getSwedish();
        this.email = recipient.getEmail();
    }

    public PermitDecisionRkaRecipientListingDTO(final LocalisedString name,
                                                final String email,
                                                final OrganisationNameDTO rka) {
        this.nameFI = name.getFinnish();
        this.nameSV = name.getSwedish();
        this.email = email;
        this.rka = rka;
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
