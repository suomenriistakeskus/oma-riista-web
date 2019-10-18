package fi.riista.feature.permit.decision;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.action.PermitDecisionActionRepository;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.permit.decision.publish.MostRelevantHarvestPermitLookupService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class PermitDecisionFeature {

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private MostRelevantHarvestPermitLookupService mostRelevantHarvestPermitLookupService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public PermitDecisionDTO getDecision(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        final PermitDecisionDocument htmlDocument = decision.getDocument() != null
                ? PermitDecisionDocumentTransformer.MARKDOWN_TO_HTML.copy(decision.getDocument())
                : new PermitDecisionDocument();
        final HarvestPermit harvestPermit = mostRelevantHarvestPermitLookupService.lookupMostRelevant(decision);

        return PermitDecisionDTO.create(decision, htmlDocument, harvestPermit,
                decision.isHandler(activeUserService.requireActiveUser()));
    }

    @Transactional(readOnly = true)
    public Long getDecisionApplicationId(final long id) {
        return permitDecisionRepository.getOne(id).getApplication().getId();
    }

    @Transactional(readOnly = true)
    public boolean hasArea(final long id) {
        return permitDecisionRepository.getOne(id).getApplication().getHarvestPermitCategory().isMooselike();
    }

    @Transactional
    public void assignApplication(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);

        if (decision.getActions().isEmpty() && decision.getApplication().getHarvestPermitCategory().isMooselike()) {
            // First handler -> Create default action text for decision
            final PermitDecisionAction action = new PermitDecisionAction();
            action.setPermitDecision(decision);
            action.setPointOfTime(DateUtil.now());
            action.setActionType(PermitDecisionAction.ActionType.KUULEMINEN);
            action.setText(Locales.isSwedish(decision.getLocale())
                    ? "Finlands viltcentral har med stöd av jaktlagen 26 § 2 moment hört regionala intressegrupper" +
                    " för att trafikskador och skador på jordbruk och skog ska bli beaktade."
                    : "Suomen riistakeskus on kuullut metsästyslain 26 §:n 2 momentin  nojalla liikenne-, maatalous-" +
                    " ja metsävahinkojen huomioon ottamiseksi alueellisia sidosryhmiä.");
            action.setDecisionText(action.getText());

            permitDecisionActionRepository.save(action);
        }

        // Update text
        decision.getDocument().setProcessing(permitDecisionTextService.generateProcessing(decision));

        // decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.setHandler(activeUserService.requireActiveUser());
    }


    @Transactional
    public void unassignApplication(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.setHandler(null);
    }
}
