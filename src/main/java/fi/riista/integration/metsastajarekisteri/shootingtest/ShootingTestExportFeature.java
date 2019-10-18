package fi.riista.integration.metsastajarekisteri.shootingtest;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import fi.riista.feature.storage.FileStorageService;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
public class ShootingTestExportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestExportFeature.class);

    @Resource
    private ShootingTestExportService exportService;

    @Resource
    private ShootingTestExportStorageService storageService;

    @Resource
    private FileStorageService fileStorageService;

    @PreAuthorize("hasPrivilege('EXPORT_SHOOTING_TEST_REGISTRY')")
    public ResponseEntity<byte[]> exportShootingTestRegistry() {
        return storageService.findShootingTestExportFile();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void constructAndStoreShootingTestRegistry(final LocalDate registerDate) {
        LOG.info("Starting to construct shooting test dataset as XML.");

        final Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            final byte[] xmlBytes = exportService.exportShootingTestData(registerDate);

            // Removing previously stored file and saving a new file must be done in different
            // consecutive transactions because of commit hook behaviour of FileStorageService.
            storageService.removeExistingShootingTestExportFile();
            storageService.storeShootingTestExportFile(xmlBytes);

        } catch (final Exception e) {
            LOG.info("Constructing shooting test dataset into XML failed after {} ms.", stopwatch.elapsed(MILLISECONDS));
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

        LOG.info("Constructing shooting test dataset into XML finished in {} ms.", stopwatch.elapsed(MILLISECONDS));
    }
}
