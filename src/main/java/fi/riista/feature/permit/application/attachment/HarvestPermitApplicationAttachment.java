package fi.riista.feature.permit.application.attachment;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.permit.application.HarvestPermitApplication;
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
import javax.validation.constraints.Size;
import java.net.URL;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplicationAttachment extends BaseEntity<Long> {

    public enum Type {
        SHOOTER_LIST,
        MH_AREA_PERMIT,
        OTHER
    }

    public static final String ID_COLUMN_NAME = "harvest_permit_application_attachment_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    // TODO: Liitetiedoston nimi, joka voidaan poistaa kun LH:sta tulleita hakemuksia ei enää tarvita.
    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    // TODO: Viittaus LH:n tarjoamaan liitetiedostoon
    @Column(length = 2048, columnDefinition = "TEXT") // max url length in IE
    private URL url;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Type attachmentType;

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

    public HarvestPermitApplicationAttachment() {
    }

    public HarvestPermitApplicationAttachment(final HarvestPermitApplication harvestPermitApplication,
                                              final String name,
                                              final URL url,
                                              final Type attachmentType) {
        this.harvestPermitApplication = harvestPermitApplication;
        this.name = name;
        this.url = url;
        this.attachmentType = attachmentType;
    }

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
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
