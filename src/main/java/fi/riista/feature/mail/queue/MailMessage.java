package fi.riista.feature.mail.queue;

import fi.riista.feature.common.entity.BaseEntity;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class MailMessage extends BaseEntity<Long> {

    private Long id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String fromEmail;

    @NotBlank
    @Size(max = 255)
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

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_message_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(final String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public DateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(final DateTime submitTime) {
        this.submitTime = submitTime;
    }

    public DateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(final DateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(final boolean delivered) {
        this.delivered = delivered;
    }
}
