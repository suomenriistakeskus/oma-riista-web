package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.decision.DecisionUtil;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Service
public class NominationDecisionRevisionDownloadFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private NominationDecisionRevisionRepository decisionRevisionRepository;


    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadPdf(final long decisionId,
                            final long revisionId,
                            final HttpServletResponse response) throws IOException {
        final NominationDecision permitDecision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);
        final NominationDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);

        download(response, permitDecision, revision);
    }

    private void download(final HttpServletResponse response,
                          final NominationDecision decision,
                          final NominationDecisionRevision revision) throws IOException {
        if (!Objects.equals(decision, revision.getNominationDecision())) {
            throw new IllegalStateException("invalid revision for decisionId: " + decision.getId());
        }

        final String filename = DecisionUtil.getNominationDecisionFileName(decision.getLocale(), decision.createDocumentNumber());

        fileDownloadService.downloadUsingTemporaryFile(revision.getPdfMetadata(), filename, response);
    }

}
