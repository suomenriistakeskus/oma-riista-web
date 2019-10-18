package fi.riista.feature.permit.decision;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethod;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethodRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountService;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class PermitDecisionAmendUpdater {

    @Resource
    private PermitDecisionSpeciesAmountService permitDecisionSpeciesAmountService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private PermitDecisionForbiddenMethodRepository permitDecisionForbiddenMethodRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private PermitDecisionGrantStatusService permitDecisionGrantStatusService;

    @Transactional
    public void updateDecision(final PermitDecision decision) {
        PermitDecision.amendFromApplication(decision);

        synchronizeSpeciesAmounts(decision);
        removeInvalidForbiddenSpeciesAmounts(decision);

        permitDecisionTextService.generateDefaultTextSections(decision, true);
    }

    private void synchronizeSpeciesAmounts(final PermitDecision decision) {
        // Recreate all decision species amounts, because they only contain amount and dates
        permitDecisionSpeciesAmountRepository.deleteByPermitDecision(decision);
        permitDecisionSpeciesAmountRepository.save(permitDecisionSpeciesAmountService.createSpecies(decision));
        permitDecisionGrantStatusService.updateGrantStatus(decision);
    }

    private void removeInvalidForbiddenSpeciesAmounts(final PermitDecision decision) {
        final HarvestPermitApplication application = decision.getApplication();
        final List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
        final Set<GameSpecies> applicationSpecies = F.mapNonNullsToSet(applicationSpeciesAmounts,
                HarvestPermitApplicationSpeciesAmount::getGameSpecies);

        final List<PermitDecisionForbiddenMethod> toDelete = new LinkedList<>();

        for (final PermitDecisionForbiddenMethod forbiddenMethod : permitDecisionForbiddenMethodRepository.findByPermitDecision(decision)) {
            if (!applicationSpecies.contains(forbiddenMethod.getGameSpecies())) {
                toDelete.add(forbiddenMethod);
            }
        }

        permitDecisionForbiddenMethodRepository.delete(toDelete);
    }
}
