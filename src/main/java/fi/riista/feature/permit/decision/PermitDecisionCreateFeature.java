package fi.riista.feature.permit.decision;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryRepository;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryService;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationService;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

import static fi.riista.feature.common.decision.GrantStatus.UNCHANGED;


@Component
public class PermitDecisionCreateFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionDeliveryService permitDecisionDeliveryService;

    @Resource
    private PermitDecisionDeliveryRepository permitDecisionDeliveryRepository;

    @Resource
    private PermitDecisionSpeciesAmountService permitDecisionSpeciesAmountService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private PermitDecisionGrantStatusService permitDecisionGrantStatusService;

    @Resource
    private PermitDecisionDerogationService permitDecisionDerogationService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public Long getOrCreateDecisionForApplication(final CreatePermitDecisionDTO dto) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                dto.getApplicationId(), EntityPermission.READ);

        final PermitDecision decision = permitDecisionRepository.findOneByApplication(application);

        return decision != null ? decision.getId() : createDecision(application).getId();
    }

    // Package private for testing
    /* package */ PermitDecision createDecision(final HarvestPermitApplication application) {
        final PermitDecision decision = PermitDecision.createForApplication(application);
        activeUserService.assertHasPermission(decision, EntityPermission.CREATE);
        permitDecisionRepository.save(decision);

        if (application.getHarvestPermitCategory().hasSpeciesAmount()) {
            permitDecisionSpeciesAmountRepository.saveAll(permitDecisionSpeciesAmountService.createSpecies(decision));
            permitDecisionGrantStatusService.updateGrantStatus(decision);
        } else {
            decision.setGrantStatus(UNCHANGED);
        }

        decision.setDelivery(permitDecisionDeliveryRepository.saveAll(
                permitDecisionDeliveryService.generateDeliveries(decision, Collections.emptyList())));

        if (application.getHarvestPermitCategory().isDerogation()) {
            permitDecisionDerogationService.initializeForApplication(decision);
        }

        // Generate content when data has been initialized
        permitDecisionTextService.generateDefaultTextSections(decision, true);

        return decision;
    }

}
