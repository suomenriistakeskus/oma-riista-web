package fi.riista.feature.mail.bounce;

import java.util.List;

public class AmazonSesComplaint {
    private List<AmazonSesComplainedRecipient> complainedRecipients;
    private String feedbackId;
    private String timestamp;

    public List<AmazonSesComplainedRecipient> getComplainedRecipients() {
        return complainedRecipients;
    }

    public void setComplainedRecipients(final List<AmazonSesComplainedRecipient> complainedRecipients) {
        this.complainedRecipients = complainedRecipients;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(final String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }
}
