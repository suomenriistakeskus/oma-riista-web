package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;

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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionRevisionAttachment extends LifecycleEntity<Long> {

    public static final Comparator<PermitDecisionRevisionAttachment> ATTACHMENT_COMPARATOR =
            Comparator.comparing(PermitDecisionRevisionAttachment::getOrderingNumber, nullsLast(naturalOrder()));

    public static final String ID_COLUMN_NAME = "permit_decision_revision_attachment_id";

    private Long id;

    // Päätöksen liitteen järjestysnumero
    @Min(1)
    @Max(999)
    @Column
    private Integer orderingNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permit_decision_attachment_id", nullable = false)
    private PermitDecisionAttachment decisionAttachment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permit_decision_revision_id", nullable = false)
    private PermitDecisionRevision decisionRevision;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public PermitDecisionRevisionAttachment() {
    }

    public PermitDecisionRevisionAttachment(final PermitDecisionRevision decisionRevision,
                                            final PermitDecisionAttachment decisionAttachment) {
        this.decisionRevision = Objects.requireNonNull(decisionRevision);
        this.decisionAttachment = Objects.requireNonNull(decisionAttachment);
        this.orderingNumber = decisionAttachment.getOrderingNumber();
    }

    public Integer getOrderingNumber() {
        return orderingNumber;
    }

    public void setOrderingNumber(final Integer orderingNumber) {
        this.orderingNumber = orderingNumber;
    }

    public PermitDecisionAttachment getDecisionAttachment() {
        return decisionAttachment;
    }

    public void setDecisionAttachment(final PermitDecisionAttachment decisionAttachment) {
        this.decisionAttachment = decisionAttachment;
    }

    public PermitDecisionRevision getDecisionRevision() {
        return decisionRevision;
    }

    public void setDecisionRevision(final PermitDecisionRevision decisionRevision) {
        this.decisionRevision = decisionRevision;
    }
}
