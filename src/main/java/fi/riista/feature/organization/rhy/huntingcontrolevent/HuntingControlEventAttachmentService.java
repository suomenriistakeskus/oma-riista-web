package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.ImageResizer;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.apache.poi.util.IOUtils;
import org.joda.time.Days;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class HuntingControlEventAttachmentService {

    // Cache time can be long due image cannot be changed - only deleted.
    private static final int CACHE_HEADER_MAX_AGE = Days.days(365).toStandardSeconds().getSeconds();

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private HuntingControlAttachmentRepository attachmentRepository;

    @Resource
    private ImageResizer imageResizer;

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
                linkAttachmentToEvent(event, storeAttachment(file));
            });
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Long addAttachment(final HuntingControlEvent event, final MultipartFile attachment, final UUID uuid) {
        return linkAttachmentToEvent(event, storeAttachmentWithUUID(attachment, uuid));
    }

    private Long linkAttachmentToEvent(final HuntingControlEvent event, final PersistentFileMetadata file) {
        final HuntingControlAttachment attachment = new HuntingControlAttachment(event, file);
        attachmentRepository.save(attachment);
        return attachment.getId();
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) {
        return storeAttachmentWithUUID(file, UUID.randomUUID());
    }

    private PersistentFileMetadata storeAttachmentWithUUID(final MultipartFile file, final UUID uuid) {
        try {
            return fileStorageService.storeFile(uuid, file.getBytes(), FileType.HUNTING_CONTROL_ATTACHMENT,
                                                file.getContentType(), file.getOriginalFilename());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteAttachmentsFromEvent(final long eventId) {
        final List<HuntingControlAttachment> attachments = attachmentRepository.findByHuntingControlEventId(eventId);
        attachments.forEach(this::delete);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getThumbnail(final HuntingControlAttachment attachment) throws IOException {

        final PersistentFileMetadata metadata = attachment.getAttachmentMetadata();
        final String contentType = metadata.getContentType().toLowerCase();

        if (!(contentType.startsWith("image") || contentType.startsWith("jpeg"))) {
            return ResponseEntity.notFound()
                    .lastModified(System.currentTimeMillis())
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate")
                    .build();
        }

        final byte[] photoData = fileStorageService.getBytes(metadata.getId());
        final byte[] thumbnailData = getThumbnailData(photoData);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(thumbnailData.length)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=" + CACHE_HEADER_MAX_AGE)
                .body(thumbnailData);
    }

    private byte[] getThumbnailData(final byte[] photoData) throws IOException {
        try {
            return imageResizer.resize(photoData, 48, 48, false);
        } catch (NullPointerException e) {
            final InputStream is = getClass().getResourceAsStream("/file-icon.png");
            return IOUtils.toByteArray(is);
        }
    }
}
