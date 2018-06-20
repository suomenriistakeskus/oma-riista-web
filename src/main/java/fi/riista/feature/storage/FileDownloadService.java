package fi.riista.feature.storage;

import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.ContentDispositionUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@Component
public class FileDownloadService {

    @Resource
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> download(final PersistentFileMetadata metadata) throws IOException {
        return download(metadata, metadata.getOriginalFilename());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> download(final PersistentFileMetadata metadata,
                                           final String filename) throws IOException {
        return ResponseEntity.ok()
                .headers(ContentDispositionUtil.header(filename))
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getContentSize())
                .body(fileStorageService.getBytes(metadata.getId()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void downloadUsingTemporaryFile(final PersistentFileMetadata metadata,
                                           final String filename,
                                           final HttpServletResponse response) throws IOException {
        Objects.requireNonNull(metadata, "metadata is null");
        downloadUsingTemporaryFile(metadata.getId(), filename, metadata.getContentType(), response);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void downloadUsingTemporaryFile(final UUID fileUuid,
                                           final String filename,
                                           final String contentType,
                                           final HttpServletResponse response) throws IOException {
        Objects.requireNonNull(fileUuid, "fileUuid is null");
        Objects.requireNonNull(filename, "filename is null");
        Objects.requireNonNull(contentType, "contentType is null");

        final Path tempFile = Files.createTempFile(null, null);

        try {
            fileStorageService.downloadTo(fileUuid, tempFile);
            response.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
            response.addHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(Files.size(tempFile)));
            ContentDispositionUtil.addHeader(response, filename);

            Files.copy(tempFile, response.getOutputStream());
            response.flushBuffer();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
