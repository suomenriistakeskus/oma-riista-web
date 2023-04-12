package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.PermitDecision;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class InformationRequestLog extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "information_request_log_id";

    // Factories
    public static InformationRequestLog create(@Nonnull final PermitDecision decision,
                                               @Nonnull final String permitTypeCode,
                                               @Nonnull final Long informationRequestLinkId) {
        requireNonNull(decision);
        requireNonNull(permitTypeCode);
        requireNonNull(informationRequestLinkId);

        final InformationRequestLog entity = new InformationRequestLog();
        entity.setDecision(decision);
        entity.setPermitTypeCode(permitTypeCode);
        entity.setInformationRequestLinkId(informationRequestLinkId);

        return entity;
    }

    // Attributes

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permit_decision_id", nullable = false)
    private PermitDecision decision;

    @NotNull
    @Size(min = 3, max = 3)
    @Column(length = 3, nullable = false)
    private String permitTypeCode;

    @NotNull
    @Column(nullable = false)
    private Long informationRequestLinkId;


    // Methods

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public PermitDecision getDecision() {
        return decision;
    }

    public void setDecision(final PermitDecision decision) {
        this.decision = decision;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public Long getInformationRequestLinkId() {
        return informationRequestLinkId;
    }

    public void setInformationRequestLinkId(final Long informationRequestLinkId) {
        this.informationRequestLinkId = informationRequestLinkId;
    }
}
