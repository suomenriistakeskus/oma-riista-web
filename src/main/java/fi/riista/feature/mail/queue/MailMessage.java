package fi.riista.feature.mail.queue;

import fi.riista.feature.common.entity.BaseEntity;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class MailMessage extends BaseEntity<Long> {

    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String fromEmail;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String toEmail;

    @NotBlank
    @Column(nullable = false)
    private String subject;

    @NotBlank
    @Column(columnDefinition = "text", nullable = false)
    private String body;

    @NotNull
    @Column(nullable = false)
    private DateTime submitTime;

    @NotNull
    @Column(nullable = false)
    private DateTime scheduledTime;

    @Column(nullable = false)
    private boolean delivered;

    @Column
    private DateTime deliveryTime;

    @Column
    private DateTime lastAttemptTime;

    @Column(nullable = false)
    private int failureCounter;

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="mail_message_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void markAsDelivered() {
        if (!isDelivered()) {
            setDelivered(true);
            setDeliveryTime(DateTime.now());
            setLastAttemptTime(DateTime.now());
        } else {
            throw new IllegalStateException("Already delivered id=" + getId());
        }
    }

    public void incrementFailureCounter() {
        if (!isDelivered()) {
            setFailureCounter(getFailureCounter() + 1);
            setLastAttemptTime(DateTime.now());
        } else {
            throw new IllegalStateException("Already delivered id=" + getId());
        }
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public DateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(DateTime submitTime) {
        this.submitTime = submitTime;
    }

    public DateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(DateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public DateTime getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(DateTime lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public DateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(DateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public int getFailureCounter() {
        return failureCounter;
    }

    public void setFailureCounter(int failureCounter) {
        this.failureCounter = failureCounter;
    }
}
