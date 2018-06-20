package fi.riista.feature.mail.bounce;

import com.google.common.base.MoreObjects;

public class AmazonSesBouncedRecipient {
    private String emailAddress;
    private String action;
    private String status;
    private String diagnosticCode;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }

    public void setDiagnosticCode(final String diagnosticCode) {
        this.diagnosticCode = diagnosticCode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("emailAddress", emailAddress)
                .add("action", action)
                .add("status", status)
                .add("diagnosticCode", diagnosticCode)
                .toString();
    }
}
