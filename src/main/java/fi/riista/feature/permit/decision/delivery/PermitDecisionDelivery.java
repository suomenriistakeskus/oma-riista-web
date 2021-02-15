package fi.riista.feature.permit.decision.delivery;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.PermitDecision;
import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;

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
import javax.validation.constraints.Size;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionDelivery extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecision permitDecision;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotNull
    @Column(nullable = false)
    private String name;

    @Email
    @Size(max = 255)
    @NotNull
    @Column(nullable = false)
    private String email;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_delivery_id", nullable = false)
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
