package fi.riista.feature.permit.application.derogation.reasons;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class DerogationPermitApplicationReason extends LifecycleEntity<Long> {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PermitDecisionDerogationReasonType reasonType;

    private Long id;

    /* For Hibernate*/
    public DerogationPermitApplicationReason() {
    }

    public DerogationPermitApplicationReason(final HarvestPermitApplication harvestPermitApplication,
                                             final PermitDecisionDerogationReasonType reasonType) {
        this.harvestPermitApplication = harvestPermitApplication;
        this.reasonType = reasonType;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "derogation_permit_application_reason_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public PermitDecisionDerogationReasonType getReasonType() {
        return reasonType;
    }

    public void setReasonType(final PermitDecisionDerogationReasonType reasonType) {
        this.reasonType = reasonType;
    }

}
