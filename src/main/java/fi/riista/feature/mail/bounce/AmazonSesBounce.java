package fi.riista.feature.mail.bounce;

import java.util.List;

public class AmazonSesBounce {
    private String bounceType;
    private String bounceSubType;
    private List<AmazonSesBouncedRecipient> bouncedRecipients;
    private String timestamp;
    private String feedbackId;
    private String remoteMtaIp;
    private String reportingMTA;

    public String getBounceType() {
        return bounceType;
    }

    public void setBounceType(final String bounceType) {
        this.bounceType = bounceType;
    }

    public String getBounceSubType() {
        return bounceSubType;
    }

    public void setBounceSubType(final String bounceSubType) {
        this.bounceSubType = bounceSubType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(final String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getRemoteMtaIp() {
        return remoteMtaIp;
    }

    public void setRemoteMtaIp(final String remoteMtaIp) {
        this.remoteMtaIp = remoteMtaIp;
    }

    public String getReportingMTA() {
        return reportingMTA;
    }

    public void setReportingMTA(final String reportingMTA) {
        this.reportingMTA = reportingMTA;
    }

    public List<AmazonSesBouncedRecipient> getBouncedRecipients() {
        return bouncedRecipients;
    }

    public void setBouncedRecipients(final List<AmazonSesBouncedRecipient> bouncedRecipients) {
        this.bouncedRecipients = bouncedRecipients;
    }
}
