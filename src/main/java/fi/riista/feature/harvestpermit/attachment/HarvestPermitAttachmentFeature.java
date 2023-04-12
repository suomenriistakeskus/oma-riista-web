package fi.riista.feature.harvestpermit.attachment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.download.HarvestPermitLatestDecisionRevisionService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentDTO;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentRepository;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionAttachment;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.riista.util.F.mapNullable;

@Component
public class HarvestPermitAttachmentFeature {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitAttachmentFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private PermitDecisionAttachmentRepository permitDecisionAttachmentRepository;

    @Resource
    private HarvestPermitLatestDecisionRevisionService harvestPermitLatestDecisionRevisionService;

    @Transactional(readOnly = true)
    public List<PermitDecisionAttachmentDTO> listAttachments(final long harvestPermitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);
        final PermitDecision decision = harvestPermit.getPermitDecision();

        if (decision == null) {
            return Collections.emptyList();
        }

        final List<PermitDecisionRevisionAttachment> revisionAttachments =
                harvestPermitLatestDecisionRevisionService.getLatestRevisionArchivePdfId(decision)
                        .map(PermitDecisionRevision::getSortedAttachments)
                        .map(list -> list.stream()
                                .map(a-> {
                                    // Sanity check, revisions should not have unordered attachments
                                    if (a.getOrderingNumber() == null) {
                                        LOG.warn("Unordered attachment(id:{}) found for revision(id:{})",
                                                a.getId(),
                                                mapNullable(a.getDecisionRevision(), PermitDecisionRevision::getId));
                                    }
                                    return a;
                                })
                                .filter(a -> a.getOrderingNumber() != null)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());

        return revisionAttachments.stream()
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
