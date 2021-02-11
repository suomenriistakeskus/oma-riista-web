package fi.riista.feature.common.entity;

import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.Valid;
import java.io.Serializable;

import static fi.riista.util.DateUtil.now;

@MappedSuperclass
@Access(value = AccessType.FIELD)
public abstract class LifecycleEntity<T extends Serializable> extends BaseEntity<T> {

    @Valid
    @Embedded
    private EntityLifecycleFields lifecycleFields;

    @Valid
    @Embedded
    private EntityAuditFields auditFields;

    public EntityLifecycleFields getLifecycleFields() {
        if (this.lifecycleFields == null) {
            this.lifecycleFields = new EntityLifecycleFields();
        }
        return lifecycleFields;
    }

    public EntityAuditFields getAuditFields() {
        if (auditFields == null) {
            this.auditFields = new EntityAuditFields();
        }
        return auditFields;
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

    public boolean isDeleted() {
        return getDeletionTime() != null;
    }

    public void softDelete() {
        if (!isDeleted()) {
            getLifecycleFields().setDeletionTime(now());
            getAuditFields().setDeletedByUserId(getActiveUserId());
        }
    }

    public Long getCreatedByUserId() {
        return getAuditFields().getCreatedByUserId();
    }

    public Long getModifiedByUserId() {
        return getAuditFields().getModifiedByUserId();
    }

    public Long getDeletedByUserId() {
        return getAuditFields().getDeletedByUserId();
    }

    @PrePersist
    protected void prePersist() {
        final DateTime now = now();
        final long activeUserId = getActiveUserId();

        getLifecycleFields().setCreationTime(now);
        getLifecycleFields().setModificationTime(now);

        if (activeUserId >= 0 || getAuditFields().getCreatedByUserId() == null) {
            getAuditFields().setCreatedByUserId(activeUserId);
            getAuditFields().setModifiedByUserId(activeUserId);
        }
    }

    @PreUpdate
    void preUpdate() {
        setModificationTimeToCurrentTime();

        final long activeUserId = getActiveUserId();

        if (activeUserId >= 0 || getAuditFields().getModifiedByUserId() == null) {
            getAuditFields().setModifiedByUserId(activeUserId);
        }
    }

    public void forceRevisionUpdate() {
        setModificationTimeToCurrentTime();
    }

    public void setModificationTimeToCurrentTime() {
        getLifecycleFields().setModificationTime(now());
    }
}
