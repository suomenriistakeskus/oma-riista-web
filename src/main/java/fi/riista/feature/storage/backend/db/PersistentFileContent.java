package fi.riista.feature.storage.backend.db;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.hibernate.annotations.Type;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Entity
@Access(value = AccessType.FIELD)
@Table(name = "file_content")
public class PersistentFileContent extends BaseEntity<UUID> {
    private UUID id;

    @MapsId
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_metadata_uuid", unique = true, nullable = false, updatable = false)
    private PersistentFileMetadata metadata;

    @NotNull
    @Column(name = "file_content", nullable = false)
    private byte[] content;

    @Id
    @Type(type = "uuid-char")
    @Access(value = AccessType.PROPERTY)
    @Override
    public UUID getId() {
        return id;
    }

    PersistentFileContent() {
        // For Hibernate
    }

    public PersistentFileContent(final PersistentFileMetadata metadata, final byte[] content) {
        this.id = metadata.getId();
        this.metadata = metadata;
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public void setId(final UUID uuid) {
        this.id = uuid;
    }

    public byte[] getContent() {
        return this.content;
    }

    public PersistentFileMetadata getMetadata() {
        return metadata;
    }
}
