package fi.riista.feature.account.mobile;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class MobileForgotPasswordDTO {
    @NotBlank
    @Email
    private String email;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String lang;

    public String getEmail() {
        return email;
    }

    @JsonSetter("email")
    public void setEmail(String email) {
        this.email = StringUtils.trim(email);
    }

    public String getLang() {
        return lang;
    }

    public void setLang(final String lang) {
        this.lang = lang;
    }
}
