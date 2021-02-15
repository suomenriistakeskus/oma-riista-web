package fi.riista.feature.common.decision.nomination.delivery;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionTextService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionDeliveryFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private NominationDecisionDeliveryRepository nominationDecisionDeliveryRepository;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    @Resource
    private NominationDecisionDeliveryService nominationDecisionDeliveryService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public void updateDelivery(final long decisionId, final NominationDecisionDeliveryUpdateDTO updateDto) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final List<NominationDecisionDelivery> deliveriesByDecision =
                nominationDecisionDeliveryRepository.findAllByNominationDecisionOrderById(decision);
        deliveriesByDecision.forEach(d -> d.setNominationDecision(null));
        nominationDecisionDeliveryRepository.deleteAll(deliveriesByDecision);

        nominationDecisionDeliveryRepository.saveAll(
                nominationDecisionDeliveryService.generateDeliveries(decision, updateDto.getDeliveries()));

        decision.getDocument().setDelivery(nominationDecisionTextService.generateDelivery(decision));
    }

    @Transactional(readOnly = true)
    public List<NominationDecisionDeliveryDTO> getDelivery(final long decisionId) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);

        final List<NominationDecisionDeliveryDTO> deliveries =
                nominationDecisionDeliveryRepository.findAllByNominationDecisionOrderById(decision)
                        .stream().map(d -> new NominationDecisionDeliveryDTO(d.getName(), d.getEmail()))
                        .collect(toList());

        if (deliveries.isEmpty()) {
            return singletonList(nominationDecisionDeliveryService.generateRhyDeliveryDTOs(decision));
        }
        return deliveries;
    }
}
