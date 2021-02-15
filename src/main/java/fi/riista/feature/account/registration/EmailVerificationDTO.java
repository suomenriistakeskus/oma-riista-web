package fi.riista.feature.account.registration;

import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Size;

public class EmailVerificationDTO {
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String token;

    @Size(min = 2, max = 2)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String lang;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(final String lang) {
        this.lang = lang;
    }
}
