package fi.riista.feature.permit.decision.authority;

import fi.riista.feature.common.decision.authority.DecisionRkaAuthorityDetails;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.PermitDecision;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionAuthority extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecision permitDecision;

    @Embedded
    @NotNull
    @Valid
    private DecisionRkaAuthorityDetails authorityDetails;

    public boolean isEqualTo(final PermitDecisionAuthority other) {
        return Objects.equals(this.getId(), other.getId())
                || this.getAuthorityDetails().equals(other.getAuthorityDetails());
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_authority_id", nullable = false)
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

    public void setPermitDecision(final PermitDecision permitDecision) {
        this.permitDecision = permitDecision;
    }

    public DecisionRkaAuthorityDetails getAuthorityDetails() {
        return authorityDetails;
    }

    public void setAuthorityDetails(final DecisionRkaAuthorityDetails authorityDetails) {
        this.authorityDetails = authorityDetails;
    }

}
