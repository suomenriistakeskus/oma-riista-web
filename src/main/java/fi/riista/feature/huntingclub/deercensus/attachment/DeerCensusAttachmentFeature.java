package fi.riista.feature.huntingclub.deercensus.attachment;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.huntingclub.deercensus.DeerCensus;
import fi.riista.feature.huntingclub.deercensus.DeerCensusRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DeerCensusAttachmentFeature {
    private static final Logger LOG = LoggerFactory.getLogger(DeerCensusAttachmentFeature.class);

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private DeerCensusAttachmentRepository deerCensusAttachmentRepository;

    @Resource
    private DeerCensusRepository deerCensusRepository;

    @Transactional(readOnly = true)
    public List<DeerCensusAttachmentDTO> listAttachments(final long deerCensusId) {
        final DeerCensus deerCensus = deerCensusRepository.getOne(deerCensusId);
        activeUserService.assertHasPermission(deerCensus, EntityPermission.READ);

        return deerCensus.getAttachments().stream()
                .map(DeerCensusAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeerCensusAttachmentDTO> listAttachmentsByIds(List<Long> attachmentIds) {
        List<DeerCensusAttachment> deerCensusAttachments = deerCensusAttachmentRepository.findAllById(attachmentIds);
        return deerCensusAttachments.stream()
                .filter(d -> d.getDeerCensus() == null)
                .map(DeerCensusAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = IOException.class)
    public Long addAttachment(final long deerCensusId,
                              final DeerCensusAttachment.Type attachmentType,
                              final MultipartFile file) throws IOException {
        final DeerCensus deerCensus = deerCensusRepository.getOne(deerCensusId);
        activeUserService.assertHasPermission(deerCensus, EntityPermission.UPDATE);

        final DeerCensusAttachment attachment = new DeerCensusAttachment();
        attachment.setAttachmentType(attachmentType);
        attachment.setDeerCensus(deerCensus);
        attachment.setAttachmentMetadata(storeAttachment(file));

        deerCensusAttachmentRepository.save(attachment);

        return attachment.getId();
    }

    @Transactional(rollbackFor = IOException.class)
    public Long addAttachmentWithoutDeerCensusAssociation(
            final DeerCensusAttachment.Type attachmentType,
            final MultipartFile file) throws IOException {

        activeUserService.requireActivePerson();

        final DeerCensusAttachment attachment = new DeerCensusAttachment();
        attachment.setAttachmentType(attachmentType);
        attachment.setAttachmentMetadata(storeAttachment(file));

        deerCensusAttachmentRepository.save(attachment);

        return attachment.getId();
    }
    @Transactional
    public void connectAttachmentsWithoutDeerCensusAssociation(
            Long deerCensusId,
            List<Long> deerCensusAttachmentIds) {

        if (CollectionUtils.isEmpty(deerCensusAttachmentIds)) {
            return;
        }
        final DeerCensus deerCensus = deerCensusRepository.getOne(deerCensusId);
        activeUserService.assertHasPermission(deerCensus, EntityPermission.UPDATE);

        List<DeerCensusAttachment> deerCensusAttachments = deerCensusAttachmentRepository.findAllById(deerCensusAttachmentIds);
        for (DeerCensusAttachment deerCensusAttachment : deerCensusAttachments) {
            if (deerCensusAttachment != null) {
                deerCensusAttachment.setDeerCensus(deerCensus);
            }
        }
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(), file.getBytes(),
                FileType.DEER_CENSUS_ATTACHMENT, file.getContentType(),
                file.getOriginalFilename());
    }

    @Transactional
    public void deleteAttachment(final long attachmentId) {
        final DeerCensusAttachment attachment = requireAttachment(attachmentId, EntityPermission.UPDATE);

        final UUID fileUuid = attachment.getAttachmentMetadata().getId();
        deerCensusAttachmentRepository.delete(attachment);
        fileStorageService.remove(fileUuid);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long attachmentId) throws IOException {
        final DeerCensusAttachment attachment = requireAttachment(attachmentId, EntityPermission.READ);

        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Nonnull
    private DeerCensusAttachment requireAttachment(final long attachmentId,
                                                   final EntityPermission permission) {
        final DeerCensusAttachment attachment = deerCensusAttachmentRepository.getOne(attachmentId);
        if (attachment.getDeerCensus() != null) {
            activeUserService.assertHasPermission(attachment.getDeerCensus(), permission);
        }
        return attachment;
    }
}
