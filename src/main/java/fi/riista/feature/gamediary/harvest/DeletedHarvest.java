package fi.riista.feature.gamediary.harvest;

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
public class DeletedHarvest extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long harvestId;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deletionTime;

    @NotNull
    @Column(nullable = false)
    private Long authorId;

    @NotNull
    @Column(nullable = false)
    private Long shooterId;

    // For hibernate
    DeletedHarvest() {
    }

    public DeletedHarvest(final long harvestId, final long authorId, final long shooterId) {
        this.harvestId = harvestId;
        this.authorId = authorId;
        this.shooterId = shooterId;
        this.deletionTime = now();
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deleted_harvest_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Long getHarvestId() {
        return harvestId;
    }

    public void setHarvestId(final Long harvestId) {
        this.harvestId = harvestId;
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

    public Long getShooterId() {
        return shooterId;
    }

    public void setShooterId(final Long shooterId) {
        this.shooterId = shooterId;
    }
}
