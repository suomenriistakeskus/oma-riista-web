package fi.riista.integration.metsastajarekisteri.shootingtest;

import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.OpenSSLPBECodec;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipException;

@Component
public class ShootingTestExportStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestExportStorageService.class);

    private static final String EXPORT_FILENAME = "ShootingTestRegistry.xml";

    @Resource
    private FileStorageService fileStorageService;

    private final UUID exportFileUUID;
    private final char[] encPassword;

    public ShootingTestExportStorageService(@Value("${shootingtest.export.file.uuid}") final String exportFileUUID,
                                            @Value("${shootingtest.export.file.encryptionPassword}") final String encryptionPassword) {

        this.exportFileUUID = UUID.fromString(exportFileUUID);
        this.encPassword = encryptionPassword.toCharArray();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> findShootingTestExportFile() {
        return fileStorageService.getMetadata(exportFileUUID)
                .map(metadata -> {
                    try {
                        final byte[] encrypted = fileStorageService.getBytes(exportFileUUID);
                        final byte[] decrypted = OpenSSLPBECodec.decryptAndDecompress(encrypted, encPassword);

                        return ResponseEntity.ok()
                                .headers(ContentDispositionUtil.header(EXPORT_FILENAME))
                                .contentLength(decrypted.length)
                                .contentType(MediaType.APPLICATION_XML)
                                .body(decrypted);

                    } catch (final ZipException ze) {
                        final String errMsg = "Shooting test export XML file was probably encrypted with different "
                                + "password than what is currently being used";

                        LOG.error(errMsg, ze);
                        return errorResponse();

                    } catch (final Exception e) {
                        LOG.error("Retrieving shooting test export file failed", e);

                        final SentryClient sentry = Sentry.getStoredClient();
                        if (sentry != null) {
                            sentry.sendException(e);
                        }

                        return errorResponse();
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static ResponseEntity<byte[]> errorResponse() {
        final byte[] content = "error".getBytes();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaTypeExtras.TEXT_PLAIN_UTF8)
                .contentLength(content.length)
                .body(content);
    }

    @Transactional
    public void removeExistingShootingTestExportFile() {
        if (fileStorageService.exists(exportFileUUID)) {
            fileStorageService.remove(exportFileUUID);
        }
    }

    @Transactional(rollbackFor = IOException.class)
    public void storeShootingTestExportFile(final byte[] xmlBytes) throws IOException {
        fileStorageService.storeFile(
                exportFileUUID,
                OpenSSLPBECodec.compressAndEncrypt(xmlBytes, encPassword),
                FileType.SHOOTING_TEST_EXPORT,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                EXPORT_FILENAME + ".gz.enc");
    }
}
