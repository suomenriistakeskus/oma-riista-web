package fi.riista.feature.permit.decision.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.PermitDecisionDocumentTransformer;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class PermitDecisionPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Transactional(readOnly = true)
    public String getDecisionPermitNumber(final long id) {
        final PermitDecision decision = permitDecisionRepository.getOne(id);
        final HarvestPermitApplication application = decision.getApplication();

        return application != null ? application.getPermitNumber() : null;
    }

    @Transactional(readOnly = true)
    public PermitDecisionPdfDTO getModel(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        final PermitDecisionDocument htmlDocument = decision.getDocument() != null
                ? PermitDecisionDocumentTransformer.MARKDOWN_TO_HTML.copy(decision.getDocument())
                : new PermitDecisionDocument();

        return new PermitDecisionPdfDTO(decision, htmlDocument);
    }
}
