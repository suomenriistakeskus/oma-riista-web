package fi.riista.feature.mail.queue;

import fi.riista.feature.common.entity.BaseEntity;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class MailMessageRecipient extends BaseEntity<Long> {

    private Long id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @Column
    private DateTime deliveryTime;

    @Min(0)
    @Column(nullable = false)
    private int failureCounter;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MailMessage mailMessage;

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_message_recipient_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public MailMessage getMailMessage() {
        return mailMessage;
    }

    public void setMailMessage(final MailMessage mailMessage) {
        this.mailMessage = mailMessage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public DateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(final DateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public int getFailureCounter() {
        return failureCounter;
    }

    public void setFailureCounter(final int failureCounter) {
        this.failureCounter = failureCounter;
    }
}
