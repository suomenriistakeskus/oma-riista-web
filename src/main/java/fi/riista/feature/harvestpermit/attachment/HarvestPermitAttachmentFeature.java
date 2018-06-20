package fi.riista.feature.harvestpermit.attachment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentDTO;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HarvestPermitAttachmentFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private PermitDecisionAttachmentRepository permitDecisionAttachmentRepository;

    @Transactional(readOnly = true)
    public List<PermitDecisionAttachmentDTO> listAttachments(final long harvestPermitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);
        final PermitDecision decision = harvestPermit.getPermitDecision();

        if (decision == null) {
            return Collections.emptyList();
        }

        return decision.getSortedAttachments().stream()
                .filter(a -> !a.isDeleted())
                .map(PermitDecisionAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long harvestPermitId,
                                                final long attachmentId) throws IOException {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);
        final PermitDecisionAttachment attachment = permitDecisionAttachmentRepository.getOne(attachmentId);

        if (!Objects.equals(attachment.getPermitDecision(), harvestPermit.getPermitDecision())) {
            throw new IllegalArgumentException("Attachment not linked to permit");
        }

        return fileDownloadService.download(attachment.getAttachmentMetadata());
    }
}
