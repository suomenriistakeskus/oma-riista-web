package fi.riista.integration.metsastajarekisteri.shootingtest;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.newrelic.api.agent.NewRelic;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.OpenSSLPBECodec;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.zip.ZipException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
public class ShootingTestExportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestExportFeature.class);

    private static final String EXPORT_FILENAME = "ShootingTestRegistry.xml";

    @Resource
    private ShootingTestExportService service;

    @Resource
    private FileStorageService fileStorageService;

    private final UUID exportFileUUID;
    private final char[] encPassword;

    public ShootingTestExportFeature(@Value("${shootingtest.export.file.uuid}") final String exportFileUUID,
                                     @Value("${shootingtest.export.file.encryptionPassword}") final String encryptionPassword) {

        this.exportFileUUID = UUID.fromString(exportFileUUID);
        this.encPassword = encryptionPassword.toCharArray();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_SHOOTING_TEST_REGISTRY')")
    public ResponseEntity<byte[]> exportShootingTestRegistry() {
        return fileStorageService.getMetadata(exportFileUUID)
                .map(metadata -> {
                    try {
                        final byte[] encrypted = fileStorageService.getBytes(exportFileUUID);

                        return ResponseEntity.ok()
                                .headers(ContentDispositionUtil.header(EXPORT_FILENAME))
                                .contentLength(metadata.getContentSize())
                                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                                .body(OpenSSLPBECodec.decryptAndDecompress(encrypted, encPassword));

                    } catch (final ZipException ze) {
                        final String errMsg = "Shooting test registry XML file was probably encrypted with different "
                                + "password than is currently being used";

                        LOG.error(errMsg, ze);
                        return errorResponse();

                    } catch (final Exception e) {
                        LOG.error("Exporting shooting test registry failed", e);
                        NewRelic.noticeError(e, false);

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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error".getBytes());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void constructAndStoreShootingTestRegistry(final LocalDate registerDate) {
        LOG.info("Starting to construct shooting test dataset as XML.");

        final Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            final byte[] xmlBytes = service.exportShootingTestData(registerDate);

            if (fileStorageService.exists(exportFileUUID)) {
                fileStorageService.remove(exportFileUUID);
            }

            final PersistentFileMetadata metadata = fileStorageService.storeFile(
                    exportFileUUID,
                    OpenSSLPBECodec.compressAndEncrypt(xmlBytes, encPassword),
                    FileType.SHOOTING_TEST_EXPORT,
                    MediaType.APPLICATION_XML_VALUE,
                    EXPORT_FILENAME + ".gz.enc");

            // Set size as length of plain text.
            metadata.setContentSize(xmlBytes.length);

        } catch (final Exception e) {
            LOG.info("Constructing shooting test dataset into XML failed after {} ms.", stopwatch.elapsed(MILLISECONDS));
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

        LOG.info("Constructing shooting test dataset into XML finished in {} ms.", stopwatch.elapsed(MILLISECONDS));
    }
}
