package fi.riista.feature.common.entity;

import org.hibernate.annotations.OptimisticLock;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;

@Embeddable
@Access(AccessType.FIELD)
public class EntityLifecycleFields implements Serializable {

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(nullable = false, updatable = false)
    private Date creationTime;

    @OptimisticLock(excluded = true)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(nullable = false)
    private Date modificationTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date deletionTime;

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Date getDeletionTime() {
        return deletionTime;
    }

    public void setDeletionTime(Date deletionTime) {
        this.deletionTime = deletionTime;
    }
}
