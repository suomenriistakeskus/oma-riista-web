package fi.riista.feature.common.decision.nomination.attachment;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.NominationDecision_;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.validator.constraints.SafeHtml;

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
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

@Entity
@Access(AccessType.FIELD)
public class NominationDecisionAttachment extends LifecycleEntity<Long> {

    public static final Comparator<NominationDecisionAttachment> ATTACHMENT_COMPARATOR =
            Comparator.comparing(NominationDecisionAttachment::getOrderingNumber, nullsLast(naturalOrder()));

    public static final String ID_COLUMN_NAME = "nomination_decision_attachment_id";

    private Long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    @Column
    private String description;

    // Päätöksen liitteen järjestysnumero
    @Min(1)
    @Max(999)
    @Column
    private Integer orderingNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private NominationDecision nominationDecision;

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata attachmentMetadata;


    /*package*/ NominationDecisionAttachment() {}

    public NominationDecisionAttachment(final NominationDecision nominationDecision,
                                        final PersistentFileMetadata attachmentMetadata) {
        this.nominationDecision = Objects.requireNonNull(nominationDecision);
        this.attachmentMetadata = Objects.requireNonNull(attachmentMetadata);
    }

    // ACCESSORS

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Integer getOrderingNumber() {
        return orderingNumber;
    }

    public void setOrderingNumber(final Integer orderingNumber) {
        this.orderingNumber = orderingNumber;
    }

    public NominationDecision getNominationDecision() {
        return nominationDecision;
    }

    public void setNominationDecision(final NominationDecision nominationDecision) {
        CriteriaUtils.updateInverseCollection(NominationDecision_.attachments, this, this.nominationDecision, nominationDecision);
        this.nominationDecision = nominationDecision;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
