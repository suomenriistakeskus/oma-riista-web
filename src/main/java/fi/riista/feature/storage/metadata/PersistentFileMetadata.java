package fi.riista.feature.storage.metadata;

import com.google.common.hash.HashCode;
import fi.riista.feature.common.entity.LifecycleEntity;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

@Entity
@Access(value = AccessType.FIELD)
@Table(name = "file_metadata")
public class PersistentFileMetadata extends LifecycleEntity<UUID> {
    public static PersistentFileMetadata create(final UUID uuid,
                                                final StorageType storageType,
                                                final String contentType,
                                                final long contentSize,
                                                final HashCode md5,
                                                final String originalFilename) {
        final PersistentFileMetadata metadata = new PersistentFileMetadata();

        metadata.setId(Objects.requireNonNull(uuid));
        metadata.setStorageType(Objects.requireNonNull(storageType, "Storage type is null"));
        metadata.setContentType(Objects.requireNonNull(contentType));
        metadata.setContentSize(contentSize);
        metadata.setMd5Hash(md5);
        metadata.setOriginalFilename(originalFilename);

        return metadata;
    }

    private UUID id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType;

    @Column(name = "original_file_name")
    private String originalFilename;

    @NotBlank
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_size", nullable = false)
    private long contentSize;

    @Column(name = "md5_hash")
    private String md5Hash;

    // Private / public for example. S3-bucket URL address
    @Column(name = "resource_url")
    private URL resourceUrl;

    @Override
    @Id
    @Type(type = "uuid-char")
    @Access(value = AccessType.PROPERTY)
    @Column(name = "file_metadata_uuid", nullable = false, updatable = false)
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public URL getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(final URL resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public HashCode getMd5Hash() {
        return md5Hash != null ? HashCode.fromString(this.md5Hash) : null;
    }

    public void setMd5Hash(final HashCode value) {
        this.md5Hash = value != null ? value.toString() : null;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(final long contentSize) {
        this.contentSize = contentSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(final String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(final StorageType storageType) {
        this.storageType = storageType;
    }
}
