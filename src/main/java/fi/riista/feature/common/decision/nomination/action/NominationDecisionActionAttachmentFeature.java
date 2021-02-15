package fi.riista.feature.common.decision.nomination.action;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class NominationDecisionActionAttachmentFeature {

    @Resource
    private NominationDecisionActionRepository actionRepository;

    @Resource
    private NominationDecisionActionAttachmentRepository actionAttachmentRepository;

    @Resource
    private PersistentFileMetadataRepository metadataRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Transactional(readOnly = true)
    public List<NominationDecisionActionAttachmentDTO> listAttachments(final long decisionId,
                                                                       final long decisionActionId) {
        final NominationDecisionAction decisionAction = requireAction(decisionId, decisionActionId, EntityPermission.READ);

        final List<NominationDecisionActionAttachment> actionAttachments =
                actionAttachmentRepository.findAllByNominationDecisionAction(decisionAction);

        final Map<UUID, PersistentFileMetadata> metadataMap =
                F.indexById(metadataRepository.findAllById(
                        F.mapNonNullsToList(actionAttachments, att -> att.getAttachmentMetadata().getId())));

        return actionAttachments
                .stream()
                .sorted(Comparator.comparing(NominationDecisionActionAttachment::getCreationTime))
                .map(att-> new NominationDecisionActionAttachmentDTO(att, metadataMap.get(att.getAttachmentMetadata().getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long decisionId,
                                                final long attachmentId) throws IOException {
        final NominationDecisionActionAttachment attachment = requireAttachment(attachmentId, EntityPermission.READ);
        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Transactional(rollbackFor = IOException.class)
    public void addAttachment(final long decisionId,
                              final long decisionActionId,
                              final MultipartFile file) throws IOException {
        final NominationDecisionAction decisionAction = requireAction(decisionId, decisionActionId, EntityPermission.UPDATE);
        decisionAction.getNominationDecision().assertHandler(activeUserService.requireActiveUser());

        final NominationDecisionActionAttachment attachment =
                new NominationDecisionActionAttachment(decisionAction, storeAttachment(file));

        actionAttachmentRepository.save(attachment);
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) throws IOException {
        return fileStorageService.storeFile(UUID.randomUUID(), file.getBytes(),
                FileType.DECISION_ACTION_ATTACHMENT, file.getContentType(),
                file.getOriginalFilename());
    }

    @Transactional
    public void deleteAttachment(final long decisionId, final long attachmentId) {
        final NominationDecisionActionAttachment attachment = requireAttachment(attachmentId, EntityPermission.UPDATE);
        final NominationDecision nominationDecision = attachment.getNominationDecisionAction().getNominationDecision();
        checkArgument(decisionId == nominationDecision.getId());
        nominationDecision.assertHandler(activeUserService.requireActiveUser());

        final UUID metadataId = attachment.getAttachmentMetadata().getId();

        attachment.setNominationDecisionAction(null);
        actionAttachmentRepository.delete(attachment);
        fileStorageService.remove(metadataId);
    }

    @Nonnull
    private NominationDecisionAction requireAction(final long decisionId,
                                                   final long decisionActionId,
                                                   final EntityPermission permission) {
        final NominationDecisionAction decisionAction = actionRepository.getOne(decisionActionId);
        final NominationDecision nominationDecision = decisionAction.getNominationDecision();
        checkArgument(decisionId == nominationDecision.getId());
        activeUserService.assertHasPermission(nominationDecision, permission);
        return decisionAction;
    }

    @Nonnull
    private NominationDecisionActionAttachment requireAttachment(final long attachmentId,
                                                                 final EntityPermission permission) {
        final NominationDecisionActionAttachment attachment =
                actionAttachmentRepository.getOne(attachmentId);
        activeUserService.assertHasPermission(attachment.getNominationDecisionAction().getNominationDecision(),
                permission);
        return attachment;
    }
}
