package fi.riista.feature.sms.storage;

import com.google.common.base.Throwables;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.EntityLifecycleFields;
import fi.riista.util.DateUtil;
import fi.riista.validation.PhoneNumber;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
@Table(name = "sms_message")
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
    @Column(nullable = false)
    private DateTime statusTimestamp;

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

    @Valid
    @Embedded
    private EntityLifecycleFields lifecycleFields;

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_message_id", nullable = false)
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
        final DateTime now = DateUtil.now();

        getLifecycleFields().setCreationTime(now);
        getLifecycleFields().setModificationTime(now);
    }

    @PreUpdate
    void preUpdate() {
        getLifecycleFields().setModificationTime(DateUtil.now());
    }

    public DateTime getCreationTime() {
        return getLifecycleFields().getCreationTime();
    }

    public DateTime getModificationTime() {
        return getLifecycleFields().getModificationTime();
    }

    public DateTime getDeletionTime() {
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
        this.statusTimestamp = DateUtil.now();
        this.statusMessage = statusMessage;
    }

    public void setErrorStatus(Throwable ex) {
        this.status = SMSMessageStatus.ERROR;
        this.statusTimestamp = DateUtil.now();
        this.statusMessage = "Caught exception " + ex.getClass().getName() + " : " +
                Throwables.getRootCause(ex).getMessage();
    }

    public DateTime getStatusTimestamp() {
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
