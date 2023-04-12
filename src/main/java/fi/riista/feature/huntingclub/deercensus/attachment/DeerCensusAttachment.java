package fi.riista.feature.huntingclub.deercensus.attachment;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.deercensus.DeerCensus;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class DeerCensusAttachment extends LifecycleEntity<Long> {

    public enum Type {
        WHITE_TAIL_DEER,
        ROE_DEER,
        FALLOW_DEER
    }

    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private DeerCensus deerCensus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type attachmentType;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata attachmentMetadata;

    public DeerCensusAttachment() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deer_census_attachment_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public DeerCensus getDeerCensus() {
        return deerCensus;
    }

    public void setDeerCensus(DeerCensus deerCensus) {
        this.deerCensus = deerCensus;
    }

    public Type getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(Type attachmentType) {
        this.attachmentType = attachmentType;
    }

    public PersistentFileMetadata getAttachmentMetadata() {
        return attachmentMetadata;
    }

    public void setAttachmentMetadata(final PersistentFileMetadata attachmentMetadata) {
        this.attachmentMetadata = attachmentMetadata;
    }
}
