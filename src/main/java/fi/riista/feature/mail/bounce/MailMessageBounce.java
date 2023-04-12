package fi.riista.feature.mail.bounce;

import fi.riista.feature.common.entity.BaseEntity;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class MailMessageBounce extends BaseEntity<Long> {

    public enum BounceType {
        Permanent, Transient, Undetermined
    }

    public enum BounceSubType {
        General,
        MailboxFull,
        ContentRejected,
        NoEmail,
        Suppressed,
        OnAccountSuppressionList,
        MessageTooLarge,
        AttachmentRejected,
        Undetermined
    }

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BounceType bounceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BounceSubType bounceSubType;

    @NotNull
    @Column(nullable = false)
    private DateTime bounceTimestamp;

    @Size(max = 255)
    @Column
    private String bounceFeedbackId;

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

    @Column(columnDefinition = "TEXT")
    private String recipientDiagnosticCode;

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
    @Column(name = "mail_message_bounce_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public BounceType getBounceType() {
        return bounceType;
    }

    public void setBounceType(final BounceType bounceType) {
        this.bounceType = bounceType;
    }

    public BounceSubType getBounceSubType() {
        return bounceSubType;
    }

    public void setBounceSubType(final BounceSubType bounceSubType) {
        this.bounceSubType = bounceSubType;
    }

    public DateTime getBounceTimestamp() {
        return bounceTimestamp;
    }

    public void setBounceTimestamp(final DateTime bounceTimestamp) {
        this.bounceTimestamp = bounceTimestamp;
    }

    public String getBounceFeedbackId() {
        return bounceFeedbackId;
    }

    public void setBounceFeedbackId(final String bounceFeedbackId) {
        this.bounceFeedbackId = bounceFeedbackId;
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

    public String getRecipientDiagnosticCode() {
        return recipientDiagnosticCode;
    }

    public void setRecipientDiagnosticCode(final String recipientDiagnosticCode) {
        this.recipientDiagnosticCode = recipientDiagnosticCode;
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
