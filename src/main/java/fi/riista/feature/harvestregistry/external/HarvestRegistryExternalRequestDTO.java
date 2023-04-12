package fi.riista.feature.harvestregistry.external;

import fi.riista.feature.harvestregistry.HarvestRegistryRequestDTO;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HarvestRegistryExternalRequestDTO extends HarvestRegistryRequestDTO {

    @NotNull
    private HarvestRegistryExternalRequestReason reason;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String remoteUser;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String remoteAddress;

    public HarvestRegistryExternalRequestReason getReason() {
        return reason;
    }

    public void setReason(final HarvestRegistryExternalRequestReason reason) {
        this.reason = reason;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(final String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @AssertTrue(message = "Shooter hunter number can be applied only for hunting control grounds")
    public boolean isShooterHunterNumberFieldValid() {
        return reason == HarvestRegistryExternalRequestReason.HUNTING_CONTROL ||
                getShooterHunterNumber() == null;
    }
}
