package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PermitDecisionGrantStatusService {

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateGrantStatus(final PermitDecision permitDecision) {
        permitDecision.setGrantStatus(calculateGrantStatus(permitDecision).calculate());
    }

    private PermitDecisionGrantStatusUpdater calculateGrantStatus(final PermitDecision permitDecision) {
        final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision);

        final List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(permitDecision.getApplication());

        return new PermitDecisionGrantStatusUpdater(applicationSpeciesAmounts, decisionSpeciesAmounts);
    }
}
