package fi.riista.feature.mail.bounce;

public class AmazonSesBounceNotification implements HasReceiptHandle {
    private String receiptHandle;
    private String notificationType;
    private AmazonSesBounce bounce;
    private AmazonSesOriginalMailMessage mail;

    @Override
    public String getReceiptHandle() {
        return receiptHandle;
    }

    @Override
    public void setReceiptHandle(final String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(final String notificationType) {
        this.notificationType = notificationType;
    }

    public AmazonSesBounce getBounce() {
        return bounce;
    }

    public void setBounce(final AmazonSesBounce bounce) {
        this.bounce = bounce;
    }

    public AmazonSesOriginalMailMessage getMail() {
        return mail;
    }

    public void setMail(final AmazonSesOriginalMailMessage mail) {
        this.mail = mail;
    }
}
