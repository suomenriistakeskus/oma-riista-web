package fi.riista.feature.permit.decision.derogation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41A;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41B;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
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

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public PermitDecisionDerogationReasonsDTO getDerogationReasons(Long decisionId) {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId,
                EntityPermission.READ);
        final HarvestPermitApplication application = requireNonNull(permitDecision.getApplication());

        switch (application.getHarvestPermitCategory()) {
            case BIRD: {
                return createReasonsForBird(permitDecision);
            }
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF: {
                return createReasonsForCarnivore(permitDecision);
            }
            case MAMMAL:
            case NEST_REMOVAL: {
                return createReasons(permitDecision);
            }
            default:
                return PermitDecisionDerogationReasonsDTO.of(ImmutableList.of());
        }

    }


    @Transactional
    public void updateDerogationReasons(Long decisionId, PermitDecisionDerogationReasonsDTO dto) {
        final PermitDecision decision = permitDecisionDerogationService.requireDecisionDerogationEditable(decisionId);
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        Preconditions.checkArgument(application.getHarvestPermitCategory().isDerogation());

        final List<PermitDecisionDerogationReason> byPermitDecision =
                permitDecisionDerogationReasonRepository.findByPermitDecision(decision);

        final Set<PermitDecisionDerogationReasonType> newValues =
                getCheckedValuesFrom(dto.getLawSections());
        final Set<PermitDecisionDerogationReasonType> oldValues =
                F.mapNonNullsToSet(byPermitDecision, PermitDecisionDerogationReason::getReasonType);

        final Sets.SetView<PermitDecisionDerogationReasonType> addedValues = Sets.difference(newValues, oldValues);
        final Sets.SetView<PermitDecisionDerogationReasonType> removedValues = Sets.difference(oldValues, newValues);

        final List<PermitDecisionDerogationReason> toAdd = addedValues.stream()
                .map(type -> new PermitDecisionDerogationReason(decision, type))
                .collect(toList());
        final List<PermitDecisionDerogationReason> toDelete = byPermitDecision.stream()
                .filter(r -> removedValues.contains(r.getReasonType())).collect(toList());

        permitDecisionDerogationReasonRepository.saveAll(toAdd);
        permitDecisionDerogationReasonRepository.deleteAll(toDelete);

        final PermitDecisionDocument document = decision.getDocument();
        document.setDecision(permitDecisionTextService.generateDecision(decision));
    }

    private PermitDecisionDerogationReasonsDTO createReasonsForBird(final PermitDecision permitDecision) {
        final List<PermitDecisionDerogationReason> byPermitDecision =
                permitDecisionDerogationReasonRepository.findByPermitDecision(permitDecision);

        final List<PermitDecisionDerogationReasonDTO> list = PermitDecisionDerogationReasonDTO.toDTOs(
                F.mapNonNullsToSet(byPermitDecision, PermitDecisionDerogationReason::getReasonType),
                SECTION_41B);
        final PermitDecisionDerogationLawSectionDTO lawSectionDTO =
                PermitDecisionDerogationLawSectionDTO.of(SECTION_41B, list);
        return PermitDecisionDerogationReasonsDTO.of(ImmutableList.of(lawSectionDTO));
    }

    private PermitDecisionDerogationReasonsDTO createReasonsForCarnivore(final PermitDecision permitDecision) {
        final List<PermitDecisionDerogationReason> byPermitDecision =
                permitDecisionDerogationReasonRepository.findByPermitDecision(permitDecision);

        final List<PermitDecisionDerogationReasonDTO> list =
                PermitDecisionDerogationReasonDTO.toDTOsForPopulationPreservation(
                        F.mapNonNullsToSet(byPermitDecision, PermitDecisionDerogationReason::getReasonType));
        final PermitDecisionDerogationLawSectionDTO lawSectionDTO =
                PermitDecisionDerogationLawSectionDTO.of(SECTION_41A, list);
        return PermitDecisionDerogationReasonsDTO.of(ImmutableList.of(lawSectionDTO));
    }

    private PermitDecisionDerogationReasonsDTO createReasons(final PermitDecision permitDecision) {
        final Map<DerogationLawSection, List<Integer>> groupedSpecies =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision).stream()
                        .map(spa -> spa.getGameSpecies().getOfficialCode())
                        .collect(groupingBy(DerogationLawSection::getSpeciesLawSection));
        final ArrayList<PermitDecisionDerogationReasonType> reasonTypes =
                F.mapNonNullsToList(permitDecisionDerogationReasonRepository.findByPermitDecision(permitDecision),
                        PermitDecisionDerogationReason::getReasonType);

        final List<PermitDecisionDerogationLawSectionDTO> lawSectionDTOS = groupedSpecies.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey()))
                .map(e -> {
                    final List<PermitDecisionDerogationReasonDTO> reasons =
                            PermitDecisionDerogationReasonDTO.toDTOs(reasonTypes, e.getKey());
                    return PermitDecisionDerogationLawSectionDTO.of(e.getKey(), reasons);
                }).collect(toList());

        return PermitDecisionDerogationReasonsDTO.of(lawSectionDTOS);
    }


    private static Set<PermitDecisionDerogationReasonType> getCheckedValuesFrom(List<PermitDecisionDerogationLawSectionDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .flatMap(sectionDTO -> sectionDTO.getReasons().stream())
                .filter(PermitDecisionDerogationReasonDTO::isChecked)
                .map(PermitDecisionDerogationReasonDTO::getReasonType)
                .collect(Collectors.toSet());
    }
}
