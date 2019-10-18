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
    private static final String FILE_NAME_PDF_MML = "kiinteistöt.pdf";

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

    @Transactional(readOnly = true)
    public PermitApplicationArchiveDTO getDataForArchive(final long applicationId) {
        return PermitApplicationArchiveDTO.create(harvestPermitApplicationRepository.getOne(applicationId));
    }

    // No transaction here as intended
    public Path createArchive(final PermitApplicationArchiveDTO dto) throws Exception {
        try (final PermitApplicationArchiveGenerator archiveGenerator = new PermitApplicationArchiveGenerator()) {
            final Path applicationPdf = archiveGenerator.addAttachment(FILENAME_PDF_APPLICATION);

            // Application
            permitApplicationArchiveExportService.exportApplicationPdf(dto, applicationPdf);
            permitApplicationValidationService.validateApplicationPdf(dto.getId(), applicationPdf);

            if (dto.isHasPermitArea()) {
                // Map GeoJSON
                final Path applicationGeoJson = archiveGenerator.addAttachment(FILENAME_GEOJSON);
                permitApplicationArchiveExportService.exportMapGeoJson(dto.getId(), applicationGeoJson);

                // Map PDF
                final Path applicationMapPdf = archiveGenerator.addAttachment(FILENAME_PDF_MAP);
                permitApplicationArchiveExportService.exportMapPdf(dto.getId(), applicationMapPdf);
                permitApplicationValidationService.validateMapPdf(applicationMapPdf);

                final Path mml = archiveGenerator.addAttachment(FILE_NAME_PDF_MML);
                permitApplicationArchiveExportService.exportMmlPdf(dto, mml);
                permitApplicationValidationService.validateMmlPdf(mml);
            }

            // Attachments
            permitApplicationArchiveExportService.exportApplicationAttachments(dto.getId(), archiveGenerator);

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
