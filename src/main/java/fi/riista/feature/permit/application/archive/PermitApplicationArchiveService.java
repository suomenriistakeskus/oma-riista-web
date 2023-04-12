package fi.riista.feature.permit.application.archive;

import com.google.common.base.Preconditions;
import fi.riista.config.Constants;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.MediaTypeExtras;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
public class PermitApplicationArchiveService {
    private static final Logger LOG = LoggerFactory.getLogger(PermitApplicationArchiveService.class);

    private static final String FILENAME_PDF_APPLICATION = "hakemus.pdf";
    private static final String FILENAME_GEOJSON = "hakemus.json";
    private static final String FILENAME_PDF_MAP = "kartta.pdf";
    private static final String FILE_NAME_PDF_MML = "kiinteistöt.pdf";
    private static final String FILENAME_PARTNER_MAP_PDF = "osakaskartta.pdf";

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
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);
        Preconditions.checkArgument(application.getStatus() == HarvestPermitApplication.Status.ACTIVE);
        return PermitApplicationArchiveDTO.create(application);
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

                final Path partnerMapPdf = archiveGenerator.addAttachment(FILENAME_PARTNER_MAP_PDF);
                permitApplicationArchiveExportService.exportPartnerMapPdf(dto.getId(), partnerMapPdf);
            }

            // Attachments
            permitApplicationArchiveExportService.exportApplicationAttachments(dto.getId(), archiveGenerator);

            return archiveGenerator.buildArchive();
        }
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public Path getOriginalZipArchive(final long applicationId) throws IOException {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);
        Preconditions.checkArgument(application.getStatus() == HarvestPermitApplication.Status.ACTIVE ||
                application.getStatus() == HarvestPermitApplication.Status.AMENDING);

        final PermitApplicationArchive original = getOriginalArchive(application);

        final PersistentFileMetadata metadata = original.getFileMetadata();
        LOG.info("Updating archive for application {}, original resource url {}", applicationId, metadata.getResourceUrl().getPath());

        final Path originalFile = Files.createTempFile("original", ".zip");
        fileStorageService.downloadTo(original.getFileMetadata().getId(), originalFile);

        return originalFile;
    }

    public Path appendPartnersMapToArchive(final Path originalFile, final long applicationId) throws IOException {
        final Path archivePath = Files.createTempFile("application", ".zip");

        try (final FileOutputStream fos = new FileOutputStream(archivePath.toFile());
             final BufferedOutputStream bos = new BufferedOutputStream(fos);
             final ZipOutputStream zipOutputStream = new ZipOutputStream(bos, Constants.DEFAULT_CHARSET);
             final ZipFile originalZip = new ZipFile(originalFile.toFile())) {

            final byte[] buf = new byte[8192];
            int count = 0;
            final Enumeration<? extends ZipEntry> entries = originalZip.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                zipOutputStream.putNextEntry(entry);

                final InputStream input = originalZip.getInputStream(entry);

                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    zipOutputStream.write(buf, 0, bytesRead);
                }

                zipOutputStream.closeEntry();
                count++;
            }

            final String partnerMapArchiveFileName = String.format("%03d_%s", count, FILENAME_PARTNER_MAP_PDF);
            final Path partnerMap = Files.createTempFile(partnerMapArchiveFileName, null);
            permitApplicationArchiveExportService.exportPartnerMapPdf(applicationId, partnerMap);
            zipOutputStream.putNextEntry(new ZipEntry(partnerMapArchiveFileName));
            Files.copy(partnerMap, zipOutputStream);
            zipOutputStream.closeEntry();
        }

        return archivePath;
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

    @Transactional
    public void updateArchive(final Path tempFile, final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);
        final PersistentFileMetadata fileMetadata = storeAttachment(tempFile);

        final PermitApplicationArchive archive = getOriginalArchive(application);

        archive.setFileMetadata(fileMetadata);

        permitApplicationArchiveRepository.save(archive);
    }

    private PermitApplicationArchive getOriginalArchive(final HarvestPermitApplication application) {
        final Optional<PermitApplicationArchive> archiveOpt = permitApplicationArchiveRepository.findByHarvestPermitApplication(application)
                .stream()
                .min(Comparator.comparing(PermitApplicationArchive::getCreationTime));

        if (!archiveOpt.isPresent()) {
            throw new NotFoundException();
        }

        return archiveOpt.get();
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
