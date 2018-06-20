package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.application.HarvestPermitApplication;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplicationConflict extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication firstApplication;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication secondApplication;

    @Column
    private Long processingSeconds;

    @Column
    private Long processingPalstaSeconds;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_application_conflict_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestPermitApplicationConflict() {
    }

    public HarvestPermitApplicationConflict(final HarvestPermitApplication firstApplication,
                                            final HarvestPermitApplication secondApplication,
                                            final long processingSeconds) {
        this.firstApplication = firstApplication;
        this.secondApplication = secondApplication;
        this.processingSeconds = processingSeconds;
    }

    public HarvestPermitApplication getFirstApplication() {
        return firstApplication;
    }

    public void setFirstApplication(final HarvestPermitApplication firstApplication) {
        this.firstApplication = firstApplication;
    }

    public HarvestPermitApplication getSecondApplication() {
        return secondApplication;
    }

    public void setSecondApplication(final HarvestPermitApplication secondApplication) {
        this.secondApplication = secondApplication;
    }

    public Long getProcessingSeconds() {
        return processingSeconds;
    }

    public void setProcessingSeconds(Long processingSeconds) {
        this.processingSeconds = processingSeconds;
    }

    public Long getProcessingPalstaSeconds() {
        return processingPalstaSeconds;
    }

    public void setProcessingPalstaSeconds(Long processingPalstaSeconds) {
        this.processingPalstaSeconds = processingPalstaSeconds;
    }
}
