package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplicationConflictBatch extends LifecycleEntity<Long> {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_application_conflict_batch_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false)
    private int huntingYear;

    @Column
    private DateTime completedAt;

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public DateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(final DateTime completedAt) {
        this.completedAt = completedAt;
    }
}
