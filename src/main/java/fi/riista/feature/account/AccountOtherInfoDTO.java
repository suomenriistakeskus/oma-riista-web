package fi.riista.feature.account;

import fi.riista.util.Patterns;
import fi.riista.validation.PhoneNumber;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class AccountOtherInfoDTO {

    @Email
    private String email;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Pattern(regexp = Patterns.BY_NAME)
    private String byName;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @PhoneNumber
    private String phoneNumber;

    private boolean denyAnnouncementEmail;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(final String byName) {
        this.byName = byName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isDenyAnnouncementEmail() {
        return denyAnnouncementEmail;
    }

    public void setDenyAnnouncementEmail(final boolean denyAnnouncementEmail) {
        this.denyAnnouncementEmail = denyAnnouncementEmail;
    }
}
