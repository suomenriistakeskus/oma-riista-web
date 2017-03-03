package fi.riista.feature.account;

import fi.riista.feature.account.user.SystemUser;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class ModifyTwoFactorAuthenticationDTO {

    @NotNull
    private SystemUser.TwoFactorAuthenticationMode twoFactorAuthentication;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String twoFactorCodeUrl;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String twoFactorCode;

    public SystemUser.TwoFactorAuthenticationMode getTwoFactorAuthentication() {
        return twoFactorAuthentication;
    }

    public void setTwoFactorAuthentication(final SystemUser.TwoFactorAuthenticationMode twoFactorAuthentication) {
        this.twoFactorAuthentication = twoFactorAuthentication;
    }

    public String getTwoFactorCodeUrl() {
        return twoFactorCodeUrl;
    }

    public void setTwoFactorCodeUrl(final String twoFactorCodeUrl) {
        this.twoFactorCodeUrl = twoFactorCodeUrl;
    }

    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(final String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }
}
