package fi.riista.feature.permit.decision.revision;

import com.google.common.base.Preconditions;
import fi.riista.api.pub.PermitDecisionDownloadDTO;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class PermitDecisionRevisionDownloadFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private PermitDecisionRevisionRepository decisionRevisionRepository;

    @Resource
    private PermitDecisionAttachmentRepository permitDecisionAttachmentRepository;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadPdf(final long decisionId,
                            final long revisionId,
                            final HttpServletResponse response) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId,
                                                                                         EntityPermission.READ);
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);

        download(response, permitDecision, revision);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadPdfNoAuthorization(final long revisionId,
                                           final HttpServletResponse response) throws IOException {
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);

        download(response, revision.getPermitDecision(), revision);
    }

    private void download(final HttpServletResponse response,
                          final PermitDecision decision,
                          final PermitDecisionRevision revision) throws IOException {
        if (!Objects.equals(decision, revision.getPermitDecision())) {
            throw new IllegalStateException("invalid revision for decisionId: " + decision.getId());
        }

        final String filename = PermitDecision.getFileName(decision.getLocale(), decision.createPermitNumber());

        fileDownloadService.downloadUsingTemporaryFile(revision.getPdfMetadata(), filename, response);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadDecisionAttachmentNoAuthorization(final long revisionId, final long attachmentId,
                                                          final HttpServletResponse response) throws IOException {
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);
        final PermitDecision permitDecision = revision.getPermitDecision();
        final PermitDecisionAttachment attachment = permitDecisionAttachmentRepository.findOne(attachmentId);

        // Only allow downloading of attachments actually listed on the decision
        if ( attachment == null || attachment.getOrderingNumber() == null){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        downloadAttachment(response, permitDecision, attachment);
    }

    private void downloadAttachment(final HttpServletResponse response, final PermitDecision permitDecision,
                                    final PermitDecisionAttachment attachment) throws IOException {
        Preconditions.checkArgument(attachment.getPermitDecision().equals(permitDecision));
        fileDownloadService.downloadUsingTemporaryFile(attachment.getAttachmentMetadata(),
                                                       attachment.getAttachmentMetadata().getOriginalFilename(),
                                                       response);
    }

    @Transactional(readOnly = true)
    public PermitDecisionDownloadDTO getDownloadLinks(final UUID uuid, final long revisionId) {
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);
        final PermitDecision decision = revision.getPermitDecision();
        final List<PermitDecisionAttachment> attachments =
                permitDecisionAttachmentRepository.findListedAttachmentsByPermitDecisionRevision(revision);
        final PermitDecisionDownloadDTO.Builder builder = PermitDecisionDownloadDTO.Builder.builder()
                .withDecisionLink(permitClientUriFactory.getAbsoluteAnonymousDecisionPdfDownloadUri(uuid).toString(),
                                  decision.createPermitNumber());

        attachments.forEach(a -> builder.withAttachment(
                permitClientUriFactory.getAbsoluteAnonymousDecisionAttachmentUri(uuid, a.getId()).toString(),
                a.getDescription()));

        return builder.build();
    }
}
