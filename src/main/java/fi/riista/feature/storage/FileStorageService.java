package fi.riista.feature.storage;

import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public interface FileStorageService {
    /**
     * Get file metadata
     *
     * @param uuid file unique id
     * @return file metadata entity if found
     */
    Optional<PersistentFileMetadata> getMetadata(UUID uuid);

    /**
     * Get file data
     *
     * @param uuid file unique id
     * @return raw file data as byte array
     * @throws IOException
     * @throws java.lang.IllegalArgumentException if no metadata with given UUID is found.
     */
    byte[] getBytes(UUID uuid) throws IOException;

    void downloadTo(UUID uuid, Path targetPath) throws IOException;

    /**
     * Remove file data from storage.
     *
     * @param uuid file unique id
     * @throws java.lang.IllegalArgumentException if no metadata with given UUID is found.
     */
    void remove(UUID uuid);

    /**
     * Store file content and metadata using various storage implementations.
     *
     * @param fileType General purpose of the file is used to control storage options
     * @return File metadata entity suitable as reference from domain classes such as Thumbnail, Upload, Report etc.
     * @throws IOException
     */
    PersistentFileMetadata storeFile(
            UUID uuid,
            byte[] content,
            FileType fileType,
            String contentType,
            String originalFilename) throws IOException;

    PersistentFileMetadata storeFile(
            UUID uuid,
            File file,
            FileType fileType,
            String contentType,
            String originalFilename) throws IOException;

    /**
     * Check if file exists
     *
     * @param uuid file unique id
     * @return true if file with given id exists
     */
    boolean exists(UUID uuid);
}
