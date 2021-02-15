package fi.riista.feature.organization.rhy.huntingcontrolevent;

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
import java.util.stream.Collectors;

@Component
public class HuntingControlEventAttachmentService {
    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private HuntingControlAttachmentRepository attachmentRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void delete(final HuntingControlAttachment attachment) {
        final UUID uuid = attachment.getAttachmentMetadata().getId();
        attachmentRepository.delete(attachment);
        fileStorageService.remove(uuid);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HuntingControlAttachmentDTO> listAttachments(final HuntingControlEvent event) {
        return attachmentRepository.findByHuntingControlEvent(event).stream()
                .map(HuntingControlAttachmentDTO::create)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getAttachment(final HuntingControlAttachment attachment) throws IOException {
        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addAttachments(final HuntingControlEvent event, final List<MultipartFile> attachments) {
        if (attachments != null) {
            attachments.forEach(file -> {
                final HuntingControlAttachment attachment = new HuntingControlAttachment(event, storeAttachment(file));
                attachmentRepository.save(attachment);
            });
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public PersistentFileMetadata storeAttachment(final MultipartFile file) {
        try {
            return fileStorageService.storeFile(UUID.randomUUID(), file.getBytes(),
                    FileType.HUNTING_CONTROL_ATTACHMENT, file.getContentType(),
                    file.getOriginalFilename());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteAttachmentsFromEvent(final long eventId) {
        final List<HuntingControlAttachment> attachments = attachmentRepository.findByHuntingControlEventId(eventId);
        attachments.forEach(this::delete);
    }
}
