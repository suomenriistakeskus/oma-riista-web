package fi.riista.feature.mail.bounce;

public class AmazonSesComplaintNotification implements HasReceiptHandle {
    private String notificationType;
    private AmazonSesComplaint complaint;
    private AmazonSesOriginalMailMessage mail;
    private String receiptHandle;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(final String notificationType) {
        this.notificationType = notificationType;
    }

    public AmazonSesComplaint getComplaint() {
        return complaint;
    }

    public void setComplaint(final AmazonSesComplaint complaint) {
        this.complaint = complaint;
    }

    public AmazonSesOriginalMailMessage getMail() {
        return mail;
    }

    public void setMail(final AmazonSesOriginalMailMessage mail) {
        this.mail = mail;
    }

    @Override
    public String getReceiptHandle() {
        return receiptHandle;
    }

    @Override
    public void setReceiptHandle(final String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }
}
