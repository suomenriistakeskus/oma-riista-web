package fi.riista.feature.common.entity;

import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class EntityLifecycleFields implements Serializable {

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(nullable = false, updatable = false)
    private DateTime creationTime;

    @OptimisticLock(excluded = true)
    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(nullable = false)
    private DateTime modificationTime;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deletionTime;

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    public DateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(DateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public DateTime getDeletionTime() {
        return deletionTime;
    }

    public void setDeletionTime(DateTime deletionTime) {
        this.deletionTime = deletionTime;
    }
}
