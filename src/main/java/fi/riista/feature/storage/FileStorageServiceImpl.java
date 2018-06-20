package fi.riista.feature.storage;

import com.google.common.collect.Ordering;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import fi.riista.feature.storage.backend.FileStorageSpi;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.feature.storage.metadata.StorageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Transactional
public class FileStorageServiceImpl implements FileStorageService {

    @Resource
    private PersistentFileMetadataRepository metadataRepository;

    @Resource
    private List<FileStorageSpi> storages = Collections.emptyList();

    private static Supplier<IllegalStateException> notConfigured(final StorageType storageType) {
        return () -> new IllegalStateException("Storage is not configured: " + storageType);
    }

    private Optional<FileStorageSpi> findStorage(final StorageType storageType) {
        return storages.stream().filter(fss -> fss.getType() == storageType && fss.isConfigured()).findAny();
    }

    private FileStorageSpi getStorage(final StorageType storageType) {
        return findStorage(storageType).orElseThrow(notConfigured(storageType));
    }

    private FileStorageSpi getStorageOrFallback(final StorageType preferredType) {
        return findStorage(preferredType)
                .orElseGet(() -> {
                    final Ordering<FileStorageSpi> explicit = Ordering
                            .explicit(StorageType.AWS_S3_BUCKET, StorageType.LOCAL_FOLDER, StorageType.LOCAL_DATABASE)
                            .onResultOf(FileStorageSpi::getType);

                    return storages.stream()
                            .filter(FileStorageSpi::isConfigured)
                            .sorted(explicit)
                            .findFirst()
                            .orElseThrow(notConfigured(preferredType));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersistentFileMetadata> getMetadata(final UUID uuid) {
        return Optional.ofNullable(metadataRepository.findOne(uuid));
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public PersistentFileMetadata storeFile(final UUID uuid,
                                            final byte[] content,
                                            final FileType fileType,
                                            final String contentType,
                                            final String originalFilename) throws IOException {
        final FileStorageSpi storage = getStorageOrFallback(fileType.storageType());

        @SuppressWarnings("deprecation")
        final PersistentFileMetadata fileMetadata = metadataRepository.saveAndFlush(
                PersistentFileMetadata.create(uuid, storage.getType(), contentType, content.length,
                        Hashing.md5().hashBytes(content), originalFilename));

        storage.storeFile(fileType, fileMetadata, new ByteArrayInputStream(content));

        return fileMetadata;
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public PersistentFileMetadata storeFile(final UUID uuid,
                                            final File file,
                                            final FileType fileType,
                                            final String contentType,
                                            final String originalFilename) throws IOException {
        final FileStorageSpi storage = getStorageOrFallback(fileType.storageType());
        @SuppressWarnings("deprecation")
        final HashCode hash = Files.asByteSource(file).hash(Hashing.md5());
        final PersistentFileMetadata fileMetadata = metadataRepository.saveAndFlush(
                PersistentFileMetadata.create(uuid, storage.getType(), contentType, file.length(), hash, originalFilename));

        try (final FileInputStream fis = new FileInputStream(file);
             final BufferedInputStream bis = new BufferedInputStream(fis)) {
            storage.storeFile(fileType, fileMetadata, bis);
        }

        return fileMetadata;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(final UUID uuid) {
        return metadataRepository.findOne(uuid) != null;
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public byte[] getBytes(final UUID uuid) throws IOException {
        final PersistentFileMetadata metadata = metadataRepository.getOne(uuid);
        final FileStorageSpi fss = getStorage(metadata.getStorageType());

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            fss.retrieveFile(metadata, bos);
            return bos.toByteArray();
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadTo(final UUID uuid, final Path targetPath) throws IOException {
        final PersistentFileMetadata metadata = metadataRepository.getOne(uuid);
        final FileStorageSpi fss = getStorage(metadata.getStorageType());

        try (final FileOutputStream fos = new FileOutputStream(targetPath.toFile());
             final BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            fss.retrieveFile(metadata, bos);
        }
    }

    @Override
    @Transactional
    public void remove(final UUID uuid) {
        final PersistentFileMetadata metadata = metadataRepository.getOne(uuid);
        getStorage(metadata.getStorageType()).removeFromStorage(metadata);
        metadataRepository.delete(metadata);
    }
}
