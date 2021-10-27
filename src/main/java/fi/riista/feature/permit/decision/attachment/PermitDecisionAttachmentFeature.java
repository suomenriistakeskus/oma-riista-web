package fi.riista.feature.permit.decision.attachment;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PermitDecisionAttachmentFeature {

    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionAttachmentFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private PermitDecisionAttachmentRepository permitDecisionAttachmentRepository;

    @Transactional(readOnly = true)
    public List<PermitDecisionAttachmentDTO> listAttachments(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);

        return decision.getSortedAttachments().stream()
                .filter(a -> !a.isDeleted())
                .map(PermitDecisionAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addAttachment(final PermitDecisionAttachmentUploadDTO dto) {
        final PermitDecision decision =
                requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final PermitDecisionAttachment attachment =
                new PermitDecisionAttachment(decision, storeAttachment(dto.getFile()));
        attachment.setOrderingNumber(getNextOrderingNumber(decision));
        attachment.setDescription(dto.getDescription());

        permitDecisionAttachmentRepository.save(attachment);
    }

    @Transactional
    public void addAdditionalAttachment(final PermitDecisionAttachmentUploadDTO dto) {
        final PermitDecision decision =
                requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        // Not asserting handler here since other persons will attach these

        final PermitDecisionAttachment attachment =
                new PermitDecisionAttachment(decision, storeAttachment(dto.getFile()));
        attachment.setOrderingNumber(null);
        attachment.setDescription(dto.getDescription());

        permitDecisionAttachmentRepository.save(attachment);
    }

    private static int getNextOrderingNumber(final PermitDecision decision) {
        final int maxOrderingNumber = decision.getAttachments().stream()
                .filter(a -> !a.isDeleted() && a.getOrderingNumber() != null)
                .mapToInt(PermitDecisionAttachment::getOrderingNumber)
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

    @Transactional(rollbackFor = IOException.class)
    public void addDefaultMooseAttachment(final long decisionId) throws IOException {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final String attachmentName = new LocalisedString(
                "Saate hirvieläinten pyyntiluvan saajille 2021",
                "Följebrev till mottagarna av hjortdjurslicenser 2021")
                .getTranslation(decision.getLocale());
        final String attachmentResourceName = new LocalisedString(
                "moose-decision-attachment-fi.pdf",
                "moose-decision-attachment-sv.pdf")
                .getTranslation(decision.getLocale());
        final File file = new ClassPathResource(attachmentResourceName).getFile();

        final PersistentFileMetadata fileMetadata = fileStorageService.storeFile(UUID.randomUUID(), file,
                FileType.DECISION_ATTACHMENT, MediaType.APPLICATION_PDF_VALUE, attachmentResourceName);

        final PermitDecisionAttachment attachment = new PermitDecisionAttachment(decision, fileMetadata);
        attachment.setOrderingNumber(getNextOrderingNumber(decision));
        attachment.setDescription(attachmentName);

        permitDecisionAttachmentRepository.save(attachment);
    }

    @Transactional
    public void deleteAttachment(final long decisionId, final long attachmentId) {
        final PermitDecisionAttachment attachment = requireAttachment(decisionId, attachmentId, EntityPermission.UPDATE);
        final PermitDecision permitDecision = attachment.getPermitDecision();
        // Not asserting handler here since other persons will add/delete attachments after publishing

        attachment.softDelete();

        updateDecisionOrdering(permitDecision);
        updateDecisionText(permitDecision);
    }

    private static void updateDecisionOrdering(final PermitDecision decision) {
        int counter = 1;

        for (final PermitDecisionAttachment attachment : decision.getSortedAttachments()) {
            if (attachment.getOrderingNumber() != null){
                attachment.setOrderingNumber(attachment.isDeleted()
                    ? null
                    : counter++);
            }
        }
    }

    private void updateDecisionText(final PermitDecision decision) {
        decision.getDocument().setAttachments(permitDecisionTextService.generateAttachments(decision));
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long decisionId, final long attachmentId) throws IOException {
        final PermitDecisionAttachment attachment = requireAttachment(decisionId, attachmentId, EntityPermission.READ);

        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }

    @Transactional
    public void updateAttachmentOrder(final long decisionId,
                                      final List<Long> ordering) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final Map<Long, PermitDecisionAttachment> attachments = F.indexById(decision.getAttachments());

        // Reset
        decision.getAttachments().forEach(a -> a.setOrderingNumber(null));

        int counter = 1;

        for (final Long attachmentId : ordering) {
            final PermitDecisionAttachment attachment = attachments.get(attachmentId);
            Objects.requireNonNull(attachment, "missing attachmentId");
            Preconditions.checkArgument(!attachment.isDeleted(), "attachment is deleted");
            attachment.setOrderingNumber(counter++);
        }

        updateDecisionText(decision);
    }

    @Nonnull
    private PermitDecisionAttachment requireAttachment(final long decisionId, final long attachmentId,
                                                       final EntityPermission permission) {
        final PermitDecisionAttachment attachment = permitDecisionAttachmentRepository.getOne(attachmentId);
        final PermitDecision permitDecision = attachment.getPermitDecision();
        activeUserService.assertHasPermission(permitDecision, permission);
        Preconditions.checkArgument(permitDecision.getId() == decisionId, "Decision does not match attachment");
        return attachment;
    }

}
