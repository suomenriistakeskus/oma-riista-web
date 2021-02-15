package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachment;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.jpa.CriteriaUtils;

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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class NominationDecisionRevisionAttachment extends LifecycleEntity<Long> {

    public static final Comparator<NominationDecisionRevisionAttachment> ATTACHMENT_COMPARATOR =
            Comparator.comparing(NominationDecisionRevisionAttachment::getOrderingNumber, nullsLast(naturalOrder()));

    public static final String ID_COLUMN_NAME = "nomination_decision_revision_attachment_id";

    private Long id;

    // Päätöksen liitteen järjestysnumero
    @Min(1)
    @Max(999)
    @Column
    private Integer orderingNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nomination_decision_attachment_id", nullable = false)
    private NominationDecisionAttachment decisionAttachment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nomination_decision_revision_id", nullable = false)
    private NominationDecisionRevision decisionRevision;

    // Package private constructor for Hibernate
    /*package*/ NominationDecisionRevisionAttachment() {
    }

    public NominationDecisionRevisionAttachment(final @Nonnull NominationDecisionRevision decisionRevision,
                                                final @Nonnull NominationDecisionAttachment decisionAttachment) {
        requireNonNull(decisionRevision);
        setDecisionRevision(decisionRevision);
        this.decisionAttachment = requireNonNull(decisionAttachment);
        this.orderingNumber = decisionAttachment.getOrderingNumber();
    }

    // Accessors

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

    public Integer getOrderingNumber() {
        return orderingNumber;
    }

    public void setOrderingNumber(final Integer orderingNumber) {
        this.orderingNumber = orderingNumber;
    }

    public NominationDecisionAttachment getDecisionAttachment() {
        return decisionAttachment;
    }

    public NominationDecisionRevision getDecisionRevision() {
        return decisionRevision;
    }

    public void setDecisionRevision(final NominationDecisionRevision decisionRevision) {
        CriteriaUtils.updateInverseCollection(NominationDecisionRevision_.attachments, this, this.decisionRevision, decisionRevision);
        this.decisionRevision = decisionRevision;
    }
}
