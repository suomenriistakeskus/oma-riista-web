package fi.riista.feature.mail.bounce;

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
public class MailMessageComplaint extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @Column(nullable = false)
    private DateTime complaintTimestamp;

    @Column(columnDefinition = "TEXT")
    private String complaintFeedbackId;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String recipientEmailAddress;

    @Size(max = 255)
    @Column
    private String recipientAction;

    @Size(max = 255)
    @Column
    private String recipientStatus;

    @Column
    private DateTime mailTimestamp;

    @Size(max = 255)
    @Column
    private String mailMessageId;

    @Size(max = 255)
    @Column
    private String mailSubject;

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_message_complaint_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public DateTime getComplaintTimestamp() {
        return complaintTimestamp;
    }

    public void setComplaintTimestamp(final DateTime complaintTimestamp) {
        this.complaintTimestamp = complaintTimestamp;
    }

    public String getComplaintFeedbackId() {
        return complaintFeedbackId;
    }

    public void setComplaintFeedbackId(final String complaintFeedbackId) {
        this.complaintFeedbackId = complaintFeedbackId;
    }

    public String getRecipientEmailAddress() {
        return recipientEmailAddress;
    }

    public void setRecipientEmailAddress(final String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
    }

    public String getRecipientAction() {
        return recipientAction;
    }

    public void setRecipientAction(final String recipientAction) {
        this.recipientAction = recipientAction;
    }

    public String getRecipientStatus() {
        return recipientStatus;
    }

    public void setRecipientStatus(final String recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public DateTime getMailTimestamp() {
        return mailTimestamp;
    }

    public void setMailTimestamp(final DateTime mailTimestamp) {
        this.mailTimestamp = mailTimestamp;
    }

    public String getMailMessageId() {
        return mailMessageId;
    }

    public void setMailMessageId(final String mailMessageId) {
        this.mailMessageId = mailMessageId;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(final String mailSubject) {
        this.mailSubject = mailSubject;
    }
}
