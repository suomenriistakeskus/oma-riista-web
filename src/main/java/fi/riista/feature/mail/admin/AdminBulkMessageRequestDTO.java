package fi.riista.feature.mail.admin;

import fi.riista.feature.common.dto.DoNotValidate;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.validation.constraints.AssertTrue;

public class AdminBulkMessageRequestDTO {

    @DoNotValidate
    @NotBlank @Length(min = 5)
    private String body;

    @DoNotValidate
    @NotBlank @Length(min = 5)
    private String subject;

    private DateTime sendAfter;

    @Length(min=3)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String confirmation;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String userConfirmation;

    @AssertTrue(message = "confirmation must match")
    public boolean isConfirmationMatch() {
        // User must type in presented confirmation to verify intention to send bulk email
        return this.userConfirmation != null && this.userConfirmation.equals(this.confirmation);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public DateTime getSendAfter() {
        return sendAfter;
    }

    public void setSendAfter(DateTime sendAfter) {
        this.sendAfter = sendAfter;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    public String getUserConfirmation() {
        return userConfirmation;
    }

    public void setUserConfirmation(String userConfirmation) {
        this.userConfirmation = userConfirmation;
    }
}
