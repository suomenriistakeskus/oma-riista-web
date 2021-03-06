package fi.riista.feature.permit.application.attachment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationLockedCondition;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
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
public class HarvestPermitApplicationAttachmentFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationAttachmentFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitApplicationLockedCondition harvestPermitApplicationLockedCondition;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private HarvestPermitApplicationAttachmentRepository harvestPermitApplicationAttachmentRepository;

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationAttachmentDTO> listAttachments(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        return application.getAttachments().stream()
                .map(HarvestPermitApplicationAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long addAttachment(final long applicationId,
                              final HarvestPermitApplicationAttachment.Type attachmentType,
                              final MultipartFile file) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);
        harvestPermitApplicationLockedCondition.assertCanUpdate(application);

        final HarvestPermitApplicationAttachment attachment = new HarvestPermitApplicationAttachment();
        attachment.setAttachmentType(attachmentType);
        attachment.setHarvestPermitApplication(application);
        attachment.setAttachmentMetadata(storeAttachment(file));

        harvestPermitApplicationAttachmentRepository.save(attachment);

        return attachment.getId();
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) {
        try {
            return fileStorageService.storeFile(UUID.randomUUID(), file.getBytes(),
                    FileType.PERMIT_APPLICATION_ATTACHMENT, file.getContentType(),
                    file.getOriginalFilename());

        } catch (IOException e) {
            LOG.warn("Saving receipt failed", e);

            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteAttachment(final long attachmentId) {
        final HarvestPermitApplicationAttachment attachment = requireAttachment(attachmentId, EntityPermission.UPDATE);

        final HarvestPermitApplication application = attachment.getHarvestPermitApplication();
        harvestPermitApplicationLockedCondition.assertCanUpdate(application);

        final UUID fileUuid = attachment.getAttachmentMetadata().getId();
        harvestPermitApplicationAttachmentRepository.delete(attachment);
        fileStorageService.remove(fileUuid);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long attachmentId) throws IOException {
        final HarvestPermitApplicationAttachment attachment = requireAttachment(attachmentId, EntityPermission.READ);

        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Nonnull
    private HarvestPermitApplicationAttachment requireAttachment(final long attachmentId,
                                                                 final EntityPermission permission) {
        final HarvestPermitApplicationAttachment attachment = harvestPermitApplicationAttachmentRepository.getOne(attachmentId);
        activeUserService.assertHasPermission(attachment.getHarvestPermitApplication(), permission);
        return attachment;
    }
}
