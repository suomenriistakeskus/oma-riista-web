package fi.riista.feature.permit.decision.delivery;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionDeliveryFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionDeliveryRepository permitDecisionDeliveryRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private PermitDecisionDeliveryService permitDecisionDeliveryService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public void updateDelivery(final long decisionId, final PermitDecisionDeliveryUpdateDTO updateDto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        decision.assertHandler(activeUserService.requireActiveUser());

        permitDecisionDeliveryRepository.deleteAll(findDeliveriesByDecision(decision));

        decision.setDelivery(permitDecisionDeliveryRepository.saveAll(
                permitDecisionDeliveryService.generateDeliveries(decision, updateDto.getDeliveries())));

        decision.getDocument().setDelivery(permitDecisionTextService.generateDelivery(decision));
    }

    @Transactional(readOnly = true)
    public List<PermitDecisionDeliveryDTO> getDelivery(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        final List<PermitDecisionDeliveryDTO> deliveries = findDeliveriesByDecision(decision)
                .stream().map(d -> new PermitDecisionDeliveryDTO(d.getName(), d.getEmail()))
                .collect(toList());
        if (deliveries.isEmpty()) {
            return permitDecisionDeliveryService.generateRhyDeliveryDTOs(decision);
        }
        return deliveries;
    }

    private List<PermitDecisionDelivery> findDeliveriesByDecision(final PermitDecision decision) {
        final QPermitDecisionDelivery DELIVERY = QPermitDecisionDelivery.permitDecisionDelivery;
        return permitDecisionDeliveryRepository.findAllAsList(DELIVERY.permitDecision.eq(decision));
    }
}
