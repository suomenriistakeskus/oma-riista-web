package fi.riista.feature.otherwisedeceased;

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
public class OtherwiseDeceasedAttachmentService {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private OtherwiseDeceasedAttachmentRepository attachmentRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void delete(final OtherwiseDeceasedAttachment attachment) {
        final UUID uuid = attachment.getAttachmentMetadata().getId();
        attachmentRepository.delete(attachment);
        fileStorageService.remove(uuid);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getAttachment(final OtherwiseDeceasedAttachment attachment) throws IOException {
        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addAttachments(final OtherwiseDeceased item, final List<MultipartFile> attachments) throws IOException {
        if (attachments != null) {
            for (int i = 0; i < attachments.size(); i++) {
                final MultipartFile file = attachments.get(i);
                final OtherwiseDeceasedAttachment attachment = new OtherwiseDeceasedAttachment(item, storeAttachment(file));
                attachmentRepository.saveAndFlush(attachment);
            }
        }
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) throws IOException {
            return fileStorageService.storeFile(UUID.randomUUID(),
                                                file.getBytes(),
                                                FileType.OTHERWISE_DECEASED_ATTACHMENT,
                                                file.getContentType(),
                                                file.getOriginalFilename());
    }

}
