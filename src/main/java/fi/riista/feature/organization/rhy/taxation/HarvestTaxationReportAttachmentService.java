package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class HarvestTaxationReportAttachmentService {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private HarvestTaxationReportAttachmentRepository attachmentRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void delete(final HarvestTaxationReportAttachment attachment) {
        final UUID uuid = attachment.getFileMetadata().getId();
        attachmentRepository.delete(attachment);
        fileStorageService.remove(uuid);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getAttachment(final HarvestTaxationReportAttachment attachment) throws IOException {
        return fileDownloadService.download(attachment.getFileMetadata());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addAttachments(final HarvestTaxationReport item, final List<MultipartFile> attachments) throws IOException {
        if (attachments != null) {
            for (final MultipartFile file : attachments) {
                final HarvestTaxationReportAttachment attachment = new HarvestTaxationReportAttachment(item, storeAttachment(file));
                attachmentRepository.saveAndFlush(attachment);
            }
        }
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(),
                file.getBytes(),
                FileType.TAXATION_REPORT_ATTACHMENT,
                file.getContentType(),
                file.getOriginalFilename());
    }

}
