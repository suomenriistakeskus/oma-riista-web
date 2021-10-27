package fi.riista.feature.otherwisedeceased;

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
@Access(value = AccessType.FIELD)
public class OtherwiseDeceasedAttachment extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "otherwise_deceased_attachment_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OtherwiseDeceased otherwiseDeceased;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata attachmentMetadata;

    // Constructors

    /** For Hibernate */
    /* package */ OtherwiseDeceasedAttachment() {
    }

    public OtherwiseDeceasedAttachment(final OtherwiseDeceased otherwiseDeceased,
                                       final PersistentFileMetadata attachmentMetadata) {
        this.otherwiseDeceased = otherwiseDeceased;
        this.attachmentMetadata = attachmentMetadata;
    }

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
    public void setId(Long id) {
        this.id = id;
    }

    public OtherwiseDeceased getOtherwiseDeceased() {
        return otherwiseDeceased;
    }

    public void setOtherwiseDeceased(final OtherwiseDeceased otherwiseDeceased) {
        CriteriaUtils.updateInverseCollection(OtherwiseDeceased_.attachments, this, this.otherwiseDeceased, otherwiseDeceased);
        this.otherwiseDeceased = otherwiseDeceased;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
