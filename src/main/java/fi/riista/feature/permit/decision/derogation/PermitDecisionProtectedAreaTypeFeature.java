package fi.riista.feature.permit.decision.derogation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.BIRD;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionProtectedAreaTypeFeature {

    @Resource
    private PermitDecisionProtectedAreaTypeRepository permitDecisionProtectedAreaTypeRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionDerogationService permitDecisionDerogationService;

    @Transactional(readOnly = true)
    public PermitDecisionProtectedAreaTypesDTO getProtectedAreaTypes(Long decisionId) {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        final HarvestPermitApplication application = requireNonNull(permitDecision.getApplication());

        if (application.getHarvestPermitCategory() == BIRD) {
            final List<PermitDecisionProtectedAreaType> byPermitDecision =
                    permitDecisionProtectedAreaTypeRepository.findByPermitDecision(permitDecision);

            final List<PermitDecisionProtectedAreaTypeDTO> list = PermitDecisionProtectedAreaTypeDTO.toDTOs(
                    F.mapNonNullsToSet(byPermitDecision, PermitDecisionProtectedAreaType::getProtectedAreaType));
            return PermitDecisionProtectedAreaTypesDTO.of(list);
        }

        return PermitDecisionProtectedAreaTypesDTO.of(ImmutableList.of());
    }

    @Transactional
    public void updateProtectedAreaTypes(Long decisionId, PermitDecisionProtectedAreaTypesDTO dto) {
        final PermitDecision decision = permitDecisionDerogationService.requireDecisionDerogationEditable(decisionId);
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        Preconditions.checkArgument(application.getHarvestPermitCategory() == BIRD,
                "Derogation not supported for permit category " + application.getHarvestPermitCategory());

        final List<PermitDecisionProtectedAreaType> byPermitDecision =
                permitDecisionProtectedAreaTypeRepository.findByPermitDecision(decision);

        final Set<ProtectedAreaType> newValues =
                getCheckedValuesFrom(dto.getTypes());
        final Set<ProtectedAreaType> oldValues =
                F.mapNonNullsToSet(byPermitDecision, PermitDecisionProtectedAreaType::getProtectedAreaType);

        final Sets.SetView<ProtectedAreaType> addedValues = Sets.difference(newValues, oldValues);
        final Sets.SetView<ProtectedAreaType> removedValues = Sets.difference(oldValues, newValues);

        final List<PermitDecisionProtectedAreaType> toAdd = addedValues.stream()
                .map(type -> new PermitDecisionProtectedAreaType(decision, type))
                .collect(toList());
        final List<PermitDecisionProtectedAreaType> toDelete = byPermitDecision.stream()
                .filter(r -> removedValues.contains(r.getProtectedAreaType())).collect(toList());

        permitDecisionProtectedAreaTypeRepository.saveAll(toAdd);
        permitDecisionProtectedAreaTypeRepository.deleteAll(toDelete);
    }

    private static Set<ProtectedAreaType> getCheckedValuesFrom(List<PermitDecisionProtectedAreaTypeDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .filter(PermitDecisionProtectedAreaTypeDTO::isChecked)
                .map(PermitDecisionProtectedAreaTypeDTO::getAreaType)
                .collect(Collectors.toSet());
    }
}
