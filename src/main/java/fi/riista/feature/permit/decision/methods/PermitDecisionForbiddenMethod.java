package fi.riista.feature.permit.decision.methods;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
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

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionForbiddenMethod extends LifecycleEntity<Long> {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecision permitDecision;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GameSpecies gameSpecies;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ForbiddenMethodType method;

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_forbidden_method_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(final PermitDecision decision) {
        this.permitDecision = decision;
    }

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public ForbiddenMethodType getMethod() {
        return method;
    }

    public void setMethod(final ForbiddenMethodType forbiddenMethod) {
        this.method = forbiddenMethod;
    }
}
