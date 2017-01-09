package fi.riista.feature.sms.storage;

import com.google.common.base.Throwables;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.EntityLifecycleFields;
import fi.riista.validation.PhoneNumber;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
@Table(name="sms_message")
public class SMSPersistentMessage extends BaseEntity<Long> {
    public enum Direction {
        IN, OUT
    }

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    // Processing status data
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_code", nullable = false)
    private SMSMessageStatus status;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date statusTimestamp;

    @Column(columnDefinition = "text")
    private String statusMessage;

    @Size(max = 35)
    @PhoneNumber
    @Column(length = 35)
    private String numberFrom;

    @Size(max = 35)
    @PhoneNumber
    @Column(length = 35)
    private String numberTo;

    @Column(columnDefinition = "text")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SystemUser systemUser;

    @Embedded
    private EntityLifecycleFields lifecycleFields;

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="sms_message_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    private EntityLifecycleFields getLifecycleFields() {
        if (this.lifecycleFields == null) {
            this.lifecycleFields = new EntityLifecycleFields();
        }
        return lifecycleFields;
    }

    @PrePersist
    protected void prePersist() {
        final Date now = new Date();

        getLifecycleFields().setCreationTime(now);
        getLifecycleFields().setModificationTime(now);
    }

    @PreUpdate
    void preUpdate() {
        getLifecycleFields().setModificationTime(new Date());
    }

    public Date getCreationTime() {
        return getLifecycleFields().getCreationTime();
    }

    public Date getModificationTime() {
        return getLifecycleFields().getModificationTime();
    }

    public Date getDeletionTime() {
        return getLifecycleFields().getDeletionTime();
    }

    public SystemUser getSystemUser() {
        return systemUser;
    }

    public void setSystemUser(SystemUser systemUser) {
        this.systemUser = systemUser;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public SMSMessageStatus getStatus() {
        return status;
    }

    public void setStatus(SMSMessageStatus status, String statusMessage) {
        this.status = Objects.requireNonNull(status);
        this.statusTimestamp = new Date();
        this.statusMessage = statusMessage;
    }

    public void setErrorStatus(Throwable ex) {
        this.status = SMSMessageStatus.ERROR;
        this.statusTimestamp = new Date();
        this.statusMessage = "Caught exception " + ex.getClass().getName() + " : " +
                Throwables.getRootCause(ex).getMessage();
    }

    public Date getStatusTimestamp() {
        return statusTimestamp;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getNumberFrom() {
        return numberFrom;
    }

    public void setNumberFrom(String numberFrom) {
        this.numberFrom = numberFrom;
    }

    public String getNumberTo() {
        return numberTo;
    }

    public void setNumberTo(String numberTo) {
        this.numberTo = numberTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
