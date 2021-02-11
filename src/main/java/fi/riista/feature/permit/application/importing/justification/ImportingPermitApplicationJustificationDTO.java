package fi.riista.feature.permit.application.importing.justification;

import fi.riista.feature.permit.application.importing.ImportingPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotBlank;

import static java.util.Objects.requireNonNull;

public class ImportingPermitApplicationJustificationDTO {

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String countryOfOrigin;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String details;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String purpose;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String release;

    public ImportingPermitApplicationJustificationDTO() {
    }

    public ImportingPermitApplicationJustificationDTO(final ImportingPermitApplication importingPermitApplication) {

        requireNonNull(importingPermitApplication, "importingPermitApplication is null");

        this.countryOfOrigin = importingPermitApplication.getCountryOfOrigin();
        this.details = importingPermitApplication.getDetails();
        this.purpose = importingPermitApplication.getPurpose();
        this.release = importingPermitApplication.getRelease();
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(final String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(final String release) {
        this.release = release;
    }
}
