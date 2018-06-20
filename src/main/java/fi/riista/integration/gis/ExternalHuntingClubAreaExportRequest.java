package fi.riista.integration.gis;

import fi.riista.util.Patterns;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ExternalHuntingClubAreaExportRequest {
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String externalId;

    @NotNull
    @Pattern(regexp = Patterns.IPV4)
    private String remoteAddress;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String remoteUser;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(final String remoteUser) {
        this.remoteUser = remoteUser;
    }
}
