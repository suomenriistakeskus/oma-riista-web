package fi.riista.feature.organization.rhy.huntingcontrolevent;

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

@Entity
@Access(AccessType.FIELD)
public class HuntingControlAttachment extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingControlEvent huntingControlEvent;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true, nullable = false)
    private PersistentFileMetadata attachmentMetadata;

    public HuntingControlAttachment() {}

    public HuntingControlAttachment(final HuntingControlEvent event, final PersistentFileMetadata attachmentMetadata) {
        this.huntingControlEvent = event;
        this.attachmentMetadata = attachmentMetadata;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hunting_control_attachment_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingControlEvent getHuntingControlEvent() {
        return huntingControlEvent;
    }

    public void setHuntingControlEvent(final HuntingControlEvent huntingControlEvent) {
        CriteriaUtils.updateInverseCollection(HuntingControlEvent_.attachments, this, this.huntingControlEvent, huntingControlEvent);
        this.huntingControlEvent = huntingControlEvent;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
