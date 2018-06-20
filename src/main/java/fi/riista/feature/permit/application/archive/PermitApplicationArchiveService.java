package fi.riista.feature.permit.application.archive;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.MediaTypeExtras;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class PermitApplicationArchiveService {
    private static final String FILENAME_PDF_APPLICATION = "hakemus.pdf";
    private static final String FILENAME_GEOJSON = "hakemus.json";
    private static final String FILENAME_PDF_MAP = "kartta.pdf";

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private PermitApplicationArchiveExportService permitApplicationArchiveExportService;

    @Resource
    private PermitApplicationArchiveValidationService permitApplicationValidationService;

    @Resource
    private PermitApplicationArchiveRepository permitApplicationArchiveRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public boolean isArchiveMissing(final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);
        final List<PermitApplicationArchive> archiveList = permitApplicationArchiveRepository
                .findByHarvestPermitApplication(application);

        return archiveList.isEmpty();
    }

    // No transaction here as intended
    public Path createArchive(final long applicationId) throws Exception {
        try (final PermitApplicationArchiveGenerator archiveGenerator = new PermitApplicationArchiveGenerator()) {
            final Path applicationPdf = archiveGenerator.addAttachment(FILENAME_PDF_APPLICATION);
            final Path applicationGeoJson = archiveGenerator.addAttachment(FILENAME_GEOJSON);
            final Path applicationMapPdf = archiveGenerator.addAttachment(FILENAME_PDF_MAP);

            // Application
            permitApplicationArchiveExportService.exportApplicationPdf(applicationId, applicationPdf);
            permitApplicationValidationService.validateApplicationPdf(applicationId, applicationPdf);

            // Map GeoJSON
            permitApplicationArchiveExportService.exportMapGeoJson(applicationId, applicationGeoJson);

            // Map PDF
            permitApplicationArchiveExportService.exportMapPdf(applicationId, applicationMapPdf);
            permitApplicationValidationService.validateMapPdf(applicationMapPdf);

            // Attachments
            permitApplicationArchiveExportService.exportApplicationAttachments(applicationId, archiveGenerator);

            return archiveGenerator.buildArchive();
        }
    }

    @Transactional
    public void storeArchive(final Path tempFile, final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);
        final PersistentFileMetadata fileMetadata = storeAttachment(tempFile);

        final PermitApplicationArchive archive = new PermitApplicationArchive();
        archive.setFileMetadata(fileMetadata);
        archive.setHarvestPermitApplication(application);
        permitApplicationArchiveRepository.save(archive);
    }

    private PersistentFileMetadata storeAttachment(final Path path) {
        try {
            return fileStorageService.storeFile(UUID.randomUUID(), path.toFile(), FileType.PERMIT_APPLICATION_ARCHIVE,
                    MediaTypeExtras.APPLICATION_ZIP_VALUE, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
