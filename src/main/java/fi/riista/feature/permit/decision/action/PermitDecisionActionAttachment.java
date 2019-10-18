package fi.riista.feature.permit.decision.action;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;

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
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionActionAttachment extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "permit_decision_action_attachment_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecisionAction permitDecisionAction;

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

    public PermitDecisionActionAttachment() {
    }

    public PermitDecisionActionAttachment(final PermitDecisionAction permitDecisionAction,
                                          final PersistentFileMetadata attachmentMetadata) {
        this.permitDecisionAction = Objects.requireNonNull(permitDecisionAction);
        this.attachmentMetadata = Objects.requireNonNull(attachmentMetadata);
    }

    public PermitDecisionAction getPermitDecisionAction() {
        return permitDecisionAction;
    }

    public void setPermitDecisionAction(final PermitDecisionAction permitDecisionAction) {
        this.permitDecisionAction = permitDecisionAction;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
