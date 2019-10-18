package fi.riista.feature.permit.application.attachment;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.hibernate.validator.constraints.SafeHtml;

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
public class HarvestPermitApplicationAttachment extends BaseEntity<Long> {

    public enum Type {
        SHOOTER_LIST,
        MH_AREA_PERMIT,
        OFFICIAL_STATEMENT,
        PROTECTED_AREA,
        OTHER
    }

    public static final String ID_COLUMN_NAME = "harvest_permit_application_attachment_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type attachmentType;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata attachmentMetadata;

    @Column(columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    public HarvestPermitApplicationAttachment() {
    }

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

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
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

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
