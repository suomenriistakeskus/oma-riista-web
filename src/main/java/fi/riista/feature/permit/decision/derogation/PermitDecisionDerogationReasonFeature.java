package fi.riista.feature.permit.decision.derogation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionDerogationReasonFeature {

    @Resource
    private PermitDecisionDerogationReasonRepository permitDecisionDerogationReasonRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private PermitDecisionDerogationService permitDecisionDerogationService;

    @Transactional(readOnly = true)
    public PermitDecisionDerogationReasonsDTO getDerogationReasons(Long decisionId) {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId,
                EntityPermission.READ);
        final HarvestPermitApplication application = requireNonNull(permitDecision.getApplication());

        switch (application.getHarvestPermitCategory()) {
            case BIRD: {
                final List<PermitDecisionDerogationReason> byPermitDecision =
                        permitDecisionDerogationReasonRepository.findByPermitDecision(permitDecision);

                final List<PermitDecisionDerogationReasonDTO> list = PermitDecisionDerogationReasonDTO.toDTOsForBird(
                        F.mapNonNullsToSet(byPermitDecision, PermitDecisionDerogationReason::getReasonType));

                return PermitDecisionDerogationReasonsDTO.of(list);
            }
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF: {
                final List<PermitDecisionDerogationReason> byPermitDecision =
                        permitDecisionDerogationReasonRepository.findByPermitDecision(permitDecision);

                final List<PermitDecisionDerogationReasonDTO> list =
                        PermitDecisionDerogationReasonDTO.toDTOsForPopulationPreservation(
                                F.mapNonNullsToSet(byPermitDecision, PermitDecisionDerogationReason::getReasonType));

                return PermitDecisionDerogationReasonsDTO.of(list);
            }
            default:
                return PermitDecisionDerogationReasonsDTO.of(ImmutableList.of());
        }

    }

    @Transactional
    public void updateDerogationReasons(Long decisionId, PermitDecisionDerogationReasonsDTO dto) {
        final PermitDecision decision = permitDecisionDerogationService.requireDecisionDerogationEditable(decisionId);
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        assertDerogationTypePermit(application.getHarvestPermitCategory());

        final List<PermitDecisionDerogationReason> byPermitDecision =
                permitDecisionDerogationReasonRepository.findByPermitDecision(decision);

        final Set<PermitDecisionDerogationReasonType> newValues =
                getCheckedValuesFrom(dto.getReasons());
        final Set<PermitDecisionDerogationReasonType> oldValues =
                F.mapNonNullsToSet(byPermitDecision, PermitDecisionDerogationReason::getReasonType);

        final Sets.SetView<PermitDecisionDerogationReasonType> addedValues = Sets.difference(newValues, oldValues);
        final Sets.SetView<PermitDecisionDerogationReasonType> removedValues = Sets.difference(oldValues, newValues);

        final List<PermitDecisionDerogationReason> toAdd = addedValues.stream()
                .map(type -> new PermitDecisionDerogationReason(decision, type))
                .collect(toList());
        final List<PermitDecisionDerogationReason> toDelete = byPermitDecision.stream()
                .filter(r -> removedValues.contains(r.getReasonType())).collect(toList());

        permitDecisionDerogationReasonRepository.save(toAdd);
        permitDecisionDerogationReasonRepository.delete(toDelete);

        final PermitDecisionDocument document = decision.getDocument();
        document.setDecision(permitDecisionTextService.generateDecision(decision));
    }

    private void assertDerogationTypePermit(final HarvestPermitCategory category) {
        switch (category) {
            case BIRD:
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
                break;
            default:
                throw new IllegalArgumentException("Non-derogation permit category: " + category);
        }
    }

    private static Set<PermitDecisionDerogationReasonType> getCheckedValuesFrom(List<PermitDecisionDerogationReasonDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .filter(PermitDecisionDerogationReasonDTO::isChecked)
                .map(PermitDecisionDerogationReasonDTO::getReasonType)
                .collect(Collectors.toSet());
    }
}
