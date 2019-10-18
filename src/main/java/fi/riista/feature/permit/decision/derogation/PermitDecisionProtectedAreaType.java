package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.decision.PermitDecision;

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

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class PermitDecisionProtectedAreaType extends LifecycleEntity<Long>
{

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecision permitDecision;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProtectedAreaType protectedAreaType;

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_protected_area_type_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    /* For Hibernate */
    public PermitDecisionProtectedAreaType() {

    }

    public PermitDecisionProtectedAreaType(final PermitDecision decision, final ProtectedAreaType type) {
        this.permitDecision = requireNonNull(decision);
        this.protectedAreaType = requireNonNull(type);
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(PermitDecision permitDecision) {
        this.permitDecision = permitDecision;
    }

    public ProtectedAreaType getProtectedAreaType() {
        return protectedAreaType;
    }

    public void setProtectedAreaType(ProtectedAreaType areaType) {
        this.protectedAreaType = areaType;
    }
}
