package fi.riista.feature.mail.admin;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class AdminBulkTestMessageRequestDTO extends AdminBulkMessageRequestDTO {

    @NotBlank
    @Email
    private String testRecipient;

    public String getTestRecipient() {
        return testRecipient;
    }

    public void setTestRecipient(String testRecipient) {
        this.testRecipient = testRecipient;
    }
}
