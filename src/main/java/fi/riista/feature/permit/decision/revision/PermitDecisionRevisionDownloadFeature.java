package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class PermitDecisionRevisionDownloadFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private PermitDecisionRevisionRepository decisionRevisionRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadPdf(final long decisionId,
                            final long revisionId,
                            final HttpServletResponse response) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
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

}
