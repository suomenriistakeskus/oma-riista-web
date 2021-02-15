package fi.riista.feature.common.decision.nomination.authority;

import fi.riista.feature.common.decision.authority.DecisionRkaAuthorityDetails;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.entity.LifecycleEntity;

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
public class NominationDecisionAuthority extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private NominationDecision nominationDecision;

    @Embedded
    @NotNull
    @Valid
    private DecisionRkaAuthorityDetails authorityDetails;


    public boolean isEqualTo(final NominationDecisionAuthority other) {
        return Objects.equals(this.getId(), other.getId())
                || Objects.equals(this.getAuthorityDetails(), other.getAuthorityDetails());
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nomination_decision_authority_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public NominationDecision getNominationDecision() {
        return nominationDecision;
    }

    public void setNominationDecision(final NominationDecision nominationDecision) {
        this.nominationDecision = nominationDecision;
    }

    public DecisionRkaAuthorityDetails getAuthorityDetails() {
        return authorityDetails;
    }

    public void setAuthorityDetails(final DecisionRkaAuthorityDetails authorityDetails) {
        this.authorityDetails = authorityDetails;
    }

}
