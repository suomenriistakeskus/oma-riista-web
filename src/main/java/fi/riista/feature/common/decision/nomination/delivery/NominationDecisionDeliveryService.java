package fi.riista.feature.common.decision.nomination.delivery;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCoordinatorService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionDeliveryService {

    @Resource
    private RiistanhoitoyhdistysCoordinatorService riistanhoitoyhdistysCoordinatorService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<NominationDecisionDelivery> generateDeliveries(final NominationDecision decision,
                                                               final List<NominationDecisionDeliveryDTO> deliveryDTOs) {
        final List<NominationDecisionDelivery> deliveries = deliveryDTOs.stream()
                .map(dto -> {
                    final NominationDecisionDelivery e = new NominationDecisionDelivery();
                    e.setNominationDecision(decision);
                    e.setEmail(dto.getEmail());
                    e.setName(dto.getName());
                    return e;
                }).collect(toList());

        final NominationDecisionDeliveryDTO rhyDto = generateRhyDeliveryDTOs(decision);

        if (!existingDeliveriesContainRhy(deliveries, rhyDto)) {
            final NominationDecisionDelivery e = new NominationDecisionDelivery();
            e.setNominationDecision(decision);
            e.setEmail(rhyDto.getEmail());
            e.setName(rhyDto.getName());
            // to keep consistent ordering, insert rhy:s first
            deliveries.add(0, e);
        }

        return deliveries;
    }

    private static boolean existingDeliveriesContainRhy(final List<NominationDecisionDelivery> deliveries,
                                                        final NominationDecisionDeliveryDTO rhy) {

        return deliveries.stream().anyMatch(d -> d.getName().equals(rhy.getName()));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public NominationDecisionDeliveryDTO generateRhyDeliveryDTOs(final NominationDecision decision) {
        final Riistanhoitoyhdistys rhy = decision.getRhy();

        final String name = rhy.getNameLocalisation().getAnyTranslation(decision.getLocale());
        final String email = riistanhoitoyhdistysCoordinatorService.resolveRhyEmail(rhy);
        return new NominationDecisionDeliveryDTO(name, email);

    }
}
