package fi.riista.feature.storage.backend;

import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.StorageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileStorageSpi {
    boolean isConfigured();

    StorageType getType();

    void storeFile(FileType fileType,
                   PersistentFileMetadata metadata,
                   InputStream inputStream) throws IOException;

    void retrieveFile(PersistentFileMetadata metadata, OutputStream outputStream) throws IOException;

    void removeFromStorage(PersistentFileMetadata metadata);
}
