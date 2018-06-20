package fi.riista.feature.permit.decision.delivery;

import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCoordinatorService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class PermitDecisionDeliveryService {

    @Resource
    private RiistanhoitoyhdistysCoordinatorService riistanhoitoyhdistysCoordinatorService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PermitDecisionDelivery> generateDeliveries(final PermitDecision decision, final List<PermitDecisionDeliveryDTO> deliveries1) {
        final List<PermitDecisionDelivery> deliveries = deliveries1.stream()
                .map(dto -> {
                    final PermitDecisionDelivery e = new PermitDecisionDelivery();
                    e.setPermitDecision(decision);
                    e.setEmail(dto.getEmail());
                    e.setName(dto.getName());
                    return e;
                }).collect(toList());

        final List<PermitDecisionDeliveryDTO> rhyDtos = generateRhyDeliveryDTOs(decision);
        rhyDtos.forEach(rhy -> {
            if (!existingDeliveriesContainRhy(deliveries, rhy)) {
                final PermitDecisionDelivery e = new PermitDecisionDelivery();
                e.setPermitDecision(decision);
                e.setEmail(rhy.getEmail());
                e.setName(rhy.getName());
                // to keep consisten ordering, insert rhy:s first
                deliveries.add(0, e);
            }
        });
        return deliveries;
    }

    private boolean existingDeliveriesContainRhy(final List<PermitDecisionDelivery> deliveries, final PermitDecisionDeliveryDTO rhy) {
        return deliveries.stream().anyMatch(d -> d.getName().equals(rhy.getName()));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PermitDecisionDeliveryDTO> generateRhyDeliveryDTOs(final PermitDecision decision) {
        return F.stream(decision.getApplication().getRhy(), decision.getApplication().getRelatedRhys())
                .map(rhy -> {
                    final String name = rhy.getNameLocalisation().getAnyTranslation(decision.getLocale());
                    final String email = riistanhoitoyhdistysCoordinatorService.resolveRhyEmail(rhy);
                    return new PermitDecisionDeliveryDTO(name, email);
                })
                .sorted(Comparator.comparing(PermitDecisionDeliveryDTO::getName))
                .collect(Collectors.toList());
    }
}
