package fi.riista.feature.storage.backend.file;

import com.google.common.base.Preconditions;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.storage.backend.FileStorageSpi;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.StorageType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalFolderFileStorage implements FileStorageSpi {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFolderFileStorage.class);

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    private Path storageBasePath;

    @PostConstruct
    public void init() throws IOException {
        final String storageFolderPath = runtimeEnvironmentUtil.getFileStorageBasePath();

        if (StringUtils.isNotBlank(storageFolderPath)) {
            LOG.info("File local storage folder: {}", storageFolderPath);

            this.storageBasePath = Paths.get(storageFolderPath);
        } else if (!runtimeEnvironmentUtil.isProductionEnvironment()) {
            LOG.warn("Using temporary folder");
            this.storageBasePath = Files.createTempDirectory("omariista").toAbsolutePath();
        } else {
            LOG.error("LocalFolderFileStorageService is not configured!");
            this.storageBasePath = null;
            return;
        }

        checkDirectoryExistsAndWritable(this.storageBasePath);
    }

    @Override
    public StorageType getType() {
        return StorageType.LOCAL_FOLDER;
    }

    @Override
    public void storeFile(final FileType fileType,
                          final PersistentFileMetadata metadata,
                          final InputStream inputStream) throws IOException {
        final Path storageFile = this.storageBasePath.resolve(fileType.formatFilename(metadata));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(final int status) {
                // Remove file if transaction is rolled back
                if (status == STATUS_ROLLED_BACK) {
                    deleteIfExists(storageFile, false);
                }
            }
        });

        metadata.setResourceUrl(storageFile.toUri().toURL());

        try {
            Files.copy(inputStream, storageFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            inputStream.close();
        }
    }

    @Override
    public void retrieveFile(final PersistentFileMetadata metadata, final OutputStream os) throws IOException {
        Files.copy(getFileStoragePath(metadata), os);
    }

    @Override
    public void removeFromStorage(PersistentFileMetadata metadata) {
        final Path storageFile = getFileStoragePath(metadata);

        // Delay until transaction has completed with success.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                deleteIfExists(storageFile, true);
            }
        });
    }

    private static void checkDirectoryExistsAndWritable(final Path directory) throws IOException {
        final File file = directory.toFile();

        if (!file.exists()) {
            Files.createDirectory(directory);
        } else {
            Preconditions.checkState(file.isDirectory(), "Storage path is not directory");
            Preconditions.checkState(file.canWrite(), "Storage path is not writable");
        }
    }

    private static void deleteIfExists(final Path storageFile, final boolean warnMissing) {
        try {
            final boolean wasDeleted = Files.deleteIfExists(storageFile);

            if (!wasDeleted && warnMissing) {
                LOG.warn("Could not delete missing file: {}.", storageFile);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getFileStoragePath(final PersistentFileMetadata metadata) {
        try {
            return Paths.get(metadata.getResourceUrl().toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Could not parse resourceUrl as File", ex);
        }
    }

    @Override
    public boolean isConfigured() {
        return storageBasePath != null;
    }
}
