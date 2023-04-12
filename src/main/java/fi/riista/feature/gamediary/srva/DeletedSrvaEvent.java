package fi.riista.feature.gamediary.srva;

import fi.riista.feature.common.entity.BaseEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import static fi.riista.util.DateUtil.now;

@Entity
@Access(value = AccessType.FIELD)
public class DeletedSrvaEvent extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long srvaEventId;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deletionTime;

    @NotNull
    @Column(nullable = false)
    private Long authorId;

    // For hibernate
    DeletedSrvaEvent() {
    }

    public DeletedSrvaEvent(final Long harvestId, final long authorId) {
        this.srvaEventId = harvestId;
        this.authorId = authorId;
        this.deletionTime = now();
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deleted_srva_event_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Long getSrvaEventId() {
        return srvaEventId;
    }

    public void setSrvaEventId(final Long srvaEventId) {
        this.srvaEventId = srvaEventId;
    }

    public DateTime getDeletionTime() {
        return deletionTime;
    }

    public void setDeletionTime(final DateTime deletionTime) {
        this.deletionTime = deletionTime;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(final Long authorId) {
        this.authorId = authorId;
    }
}
