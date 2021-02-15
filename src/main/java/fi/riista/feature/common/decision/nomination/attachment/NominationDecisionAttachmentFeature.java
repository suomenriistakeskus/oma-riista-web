package fi.riista.feature.common.decision.nomination.attachment;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionAttachment;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionTextService;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class NominationDecisionAttachmentFeature {

    private static final Logger LOG = LoggerFactory.getLogger(NominationDecisionAttachmentFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    @Resource
    private NominationDecisionAttachmentRepository nominationDecisionAttachmentRepository;

    @Transactional(readOnly = true)
    public List<NominationDecisionAttachmentDTO> listAttachments(final long decisionId) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);

        final List<NominationDecisionAttachment> attachments =
                nominationDecisionAttachmentRepository.findAllByNominationDecision(decision);

        return F.mapNonNullsToList(attachments, NominationDecisionAttachmentDTO::new);
    }

    @Transactional
    public void addAttachment(final NominationDecisionAttachmentUploadDTO dto) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final NominationDecisionAttachment attachment = createAttachment(dto, decision, getNextOrderingNumber(decision));

        nominationDecisionAttachmentRepository.save(attachment);
    }

    @Transactional
    public void addAdditionalAttachment(final NominationDecisionAttachmentUploadDTO dto) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        // Not asserting handler here since other persons will attach these

        final NominationDecisionAttachment attachment = createAttachment(dto, decision, null);

        nominationDecisionAttachmentRepository.save(attachment);
    }

    private NominationDecisionAttachment createAttachment(final NominationDecisionAttachmentUploadDTO dto,
                                                          final NominationDecision decision,
                                                          final Integer orderingNulber) {
        final NominationDecisionAttachment attachment =
                new NominationDecisionAttachment(decision, storeAttachment(dto.getFile()));
        attachment.setDescription(dto.getDescription());
        attachment.setOrderingNumber(orderingNulber);
        return attachment;
    }

    private int getNextOrderingNumber(final NominationDecision decision) {
        final List<NominationDecisionAttachment> attachments =
                nominationDecisionAttachmentRepository.findOrderedByNominationDecision(decision);

        final int maxOrderingNumber = attachments.stream()
                .mapToInt(NominationDecisionAttachment::getOrderingNumber)
                .max().orElse(0);
        return maxOrderingNumber + 1;
    }

    private PersistentFileMetadata storeAttachment(final MultipartFile file) {
        try {
            return fileStorageService.storeFile(UUID.randomUUID(), file.getBytes(),
                    FileType.DECISION_ATTACHMENT, file.getContentType(),
                    file.getOriginalFilename());

        } catch (IOException e) {
            LOG.warn("Saving receipt failed", e);

            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteAttachment(final long decisionId, final long attachmentId) {
        final NominationDecisionAttachment attachment =
                requireAttachmentByDecision(decisionId, attachmentId, EntityPermission.UPDATE);
        final NominationDecision permitDecision = attachment.getNominationDecision();
        // Not asserting handler here since other persons will add/delete attachments after publishing

        attachment.softDelete();

        updateDecisionOrdering(permitDecision);
        updateDecisionText(permitDecision);
    }

    private void updateDecisionOrdering(final NominationDecision decision) {
        int counter = 1;

        final List<NominationDecisionAttachment> attachments =
                nominationDecisionAttachmentRepository.findOrderedByNominationDecision(decision);

        for (final NominationDecisionAttachment attachment : attachments) {
            attachment.setOrderingNumber(counter++);
        }
    }

    private void updateDecisionText(final NominationDecision decision) {
        decision.getDocument().setAttachments(nominationDecisionTextService.generateAttachments(decision));
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long decisionId, final long attachmentId) throws IOException {
        final NominationDecisionAttachment attachment =
                requireAttachmentByDecision(decisionId, attachmentId, EntityPermission.READ);

        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Transactional
    public void updateAttachmentOrder(final long decisionId,
                                      final List<Long> ordering) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final List<NominationDecisionAttachment> attachments =
                nominationDecisionAttachmentRepository.findAllByNominationDecision(decision);

        // Reset
        attachments.forEach(a -> a.setOrderingNumber(null));
        final Map<Long, NominationDecisionAttachment> attachmentById = F.indexById(attachments);

        int counter = 1;

        for (final Long attachmentId : ordering) {
            final NominationDecisionAttachment attachment = attachmentById.get(attachmentId);
            Objects.requireNonNull(attachment, "missing attachmentId");
            attachment.setOrderingNumber(counter++);
        }

        updateDecisionText(decision);
    }

    @Nonnull
    private NominationDecisionAttachment requireAttachmentByDecision(final long decisionId, final long attachmentId,
                                                                     final EntityPermission permission) {
        final NominationDecisionAttachment attachment = nominationDecisionAttachmentRepository.getOne(attachmentId);
        final NominationDecision permitDecision = attachment.getNominationDecision();
        activeUserService.assertHasPermission(permitDecision, permission);
        Preconditions.checkArgument(permitDecision.getId() == decisionId, "Decision does not match attachment");
        return attachment;
    }

}
