package fi.riista.feature.common.decision.nomination.action;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.jpa.CriteriaUtils;

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

import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class NominationDecisionActionAttachment extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "nomination_decision_action_attachment_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private NominationDecisionAction nominationDecisionAction;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata attachmentMetadata;


    public NominationDecisionActionAttachment() {
    }

    public NominationDecisionActionAttachment(final NominationDecisionAction nominationDecisionAction,
                                              final PersistentFileMetadata attachmentMetadata) {
        requireNonNull(nominationDecisionAction);
        setNominationDecisionAction(nominationDecisionAction);
        this.attachmentMetadata = requireNonNull(attachmentMetadata);
    }

    // Accessors -->

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

    public NominationDecisionAction getNominationDecisionAction() {
        return nominationDecisionAction;
    }

    public void setNominationDecisionAction(final NominationDecisionAction nominationDecisionAction) {
        CriteriaUtils.updateInverseCollection(NominationDecisionAction_.attachments, this, this.nominationDecisionAction, nominationDecisionAction);
        this.nominationDecisionAction = nominationDecisionAction;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
