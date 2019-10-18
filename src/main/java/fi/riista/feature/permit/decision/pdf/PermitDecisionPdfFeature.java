package fi.riista.feature.permit.decision.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.PermitDecisionDocumentTransformer;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class PermitDecisionPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public PermitDecisionPdfFileDTO getDecisionPermitNumber(final long id) {
        final PermitDecision decision = permitDecisionRepository.getOne(id);
        final String permitNumber = getVisiblePermitNumber(decision);
        final String filename = PermitDecision.getFileName(decision.getLocale(), permitNumber);

        return new PermitDecisionPdfFileDTO(filename, permitNumber);
    }

    @Transactional(readOnly = true)
    public PermitDecisionPdfDTO getModel(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        final PermitDecisionDocument htmlDocument = decision.getDocument() != null
                ? PermitDecisionDocumentTransformer.MARKDOWN_TO_HTML.copy(decision.getDocument())
                : new PermitDecisionDocument();

        final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(decision);

        final List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(decision.getApplication());

        final String permitNumber = getVisiblePermitNumber(decision);

        return new PermitDecisionPdfDTO(permitNumber, decision, htmlDocument, decisionSpeciesAmounts, applicationSpeciesAmounts);
    }

    private String getVisiblePermitNumber(final PermitDecision decision) {
        return decision.isDraft()
                ? messageSource.getMessage("pdf.application.header.draft", null, decision.getLocale())
                : decision.createPermitNumber();
    }
}
