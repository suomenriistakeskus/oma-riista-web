package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionRevisionReceiver extends LifecycleEntity<Long> {

    public enum ReceiverType {
        CONTACT_PERSON,
        OTHER
    }

    public static final String ID_COLUMN_NAME = "permit_decision_revision_receiver_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permit_decision_revision_id", nullable = false)
    private PermitDecisionRevision decisionRevision;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiverType receiverType;

    @Email
    @Size(max = 255)
    @Column
    private String email;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private DateTime scheduledDate;

    @Column
    private DateTime sentDate;

    @Column(nullable = false)
    private boolean cancelled;

    @NotNull
    @Type(type = "uuid-char")
    @Column(nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private int viewCount;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public PermitDecisionRevisionReceiver() {
    }

    public PermitDecisionRevisionReceiver(final PermitDecisionRevision decisionRevision,
                                          final ReceiverType receiverType,
                                          final String email,
                                          final String name,
                                          final DateTime scheduledDate) {
        this.decisionRevision = Objects.requireNonNull(decisionRevision);
        this.receiverType = Objects.requireNonNull(receiverType);
        this.email = StringUtils.trimToNull(email);
        this.name = Objects.requireNonNull(StringUtils.trimToNull(name));
        this.scheduledDate = Objects.requireNonNull(scheduledDate);
        this.uuid = UUID.randomUUID();
    }

    public PermitDecisionRevision getDecisionRevision() {
        return decisionRevision;
    }

    public void setDecisionRevision(final PermitDecisionRevision decisionRevision) {
        this.decisionRevision = decisionRevision;
    }

    public ReceiverType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(final ReceiverType receiverType) {
        this.receiverType = receiverType;
    }

    public DateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(final DateTime sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final DateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(final int viewCount) {
        this.viewCount = viewCount;
    }
}
