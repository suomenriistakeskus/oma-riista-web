package fi.riista.feature.permit.decision.attachment;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
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
public class PermitDecisionAttachment extends LifecycleEntity<Long> {

    public static final Comparator<PermitDecisionAttachment> ATTACHMENT_COMPARATOR =
            Comparator.comparing(PermitDecisionAttachment::getOrderingNumber, nullsLast(naturalOrder()));

    public static final String ID_COLUMN_NAME = "permit_decision_attachment_id";

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
    private PermitDecision permitDecision;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private PersistentFileMetadata attachmentMetadata;

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

    public PermitDecisionAttachment() {
    }

    public PermitDecisionAttachment(final PermitDecision permitDecision,
                                    final PersistentFileMetadata attachmentMetadata) {
        this.permitDecision = Objects.requireNonNull(permitDecision);
        this.attachmentMetadata = Objects.requireNonNull(attachmentMetadata);
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

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(final PermitDecision permitDecision) {
        this.permitDecision = permitDecision;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
