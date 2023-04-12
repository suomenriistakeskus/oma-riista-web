package fi.riista.feature.gamediary.observation;

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
public class DeletedObservation extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long observationId;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deletionTime;

    @NotNull
    @Column(nullable = false)
    private Long authorId;

    @NotNull
    @Column(nullable = false)
    private Long observerId;

    // For hibernate
    DeletedObservation() {
    }

    public DeletedObservation(final Long observationId, final long authorId, final long observerId) {
        this.observationId = observationId;
        this.authorId = authorId;
        this.observerId = observerId;
        this.deletionTime = now();
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deleted_observation_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Long getObservationId() {
        return observationId;
    }

    public void setObservationId(final Long observationId) {
        this.observationId = observationId;
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

    public Long getObserverId() {
        return observerId;
    }

    public void setObserverId(final Long observerId) {
        this.observerId = observerId;
    }
}
