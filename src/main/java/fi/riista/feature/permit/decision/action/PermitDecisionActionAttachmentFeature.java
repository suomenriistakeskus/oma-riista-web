package fi.riista.feature.permit.decision.action;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PermitDecisionActionAttachmentFeature {

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private PermitDecisionActionAttachmentRepository permitDecisionActionAttachmentRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Transactional(readOnly = true)
    public List<PermitDecisionActionAttachmentDTO> listAttachments(final long decisionActionId) {
        final PermitDecisionAction decisionAction = requireAction(decisionActionId);

        return decisionAction.getAttachments().stream()
                .sorted(Comparator.comparing(PermitDecisionActionAttachment::getCreationTime))
                .map(PermitDecisionActionAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long attachmentId) throws IOException {
        final PermitDecisionActionAttachment attachment = requireAttachment(attachmentId, EntityPermission.READ);
        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Transactional(rollbackFor = IOException.class)
    public void addAttachment(final long decisionActionId, final MultipartFile file) throws IOException {
        final PermitDecisionAction decisionAction = requireAction(decisionActionId);
        decisionAction.getPermitDecision().assertHandler(activeUserService.requireActiveUser());

        final PermitDecisionActionAttachment attachment = new PermitDecisionActionAttachment(decisionAction,
                storeAttachment(file));

        permitDecisionActionAttachmentRepository.save(attachment);
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(), file.getBytes(),
                FileType.DECISION_ACTION_ATTACHMENT, file.getContentType(),
                file.getOriginalFilename());
    }

    @Transactional
    public void deleteAttachment(final long attachmentId) {
        final PermitDecisionActionAttachment attachment = requireAttachment(attachmentId, EntityPermission.UPDATE);
        attachment.getPermitDecisionAction().getPermitDecision().assertHandler(activeUserService.requireActiveUser());

        final UUID metadataId = attachment.getAttachmentMetadata().getId();
        permitDecisionActionAttachmentRepository.delete(attachment);
        fileStorageService.remove(metadataId);
    }

    @Nonnull
    private PermitDecisionAction requireAction(final long decisionActionId) {
        final PermitDecisionAction decisionAction = permitDecisionActionRepository.getOne(decisionActionId);
        activeUserService.assertHasPermission(decisionAction.getPermitDecision(), EntityPermission.UPDATE);
        return decisionAction;
    }

    @Nonnull
    private PermitDecisionActionAttachment requireAttachment(final long attachmentId,
                                                             final EntityPermission permission) {
        final PermitDecisionActionAttachment attachment = permitDecisionActionAttachmentRepository.getOne(attachmentId);
        activeUserService.assertHasPermission(attachment.getPermitDecisionAction().getPermitDecision(), permission);
        return attachment;
    }
}
