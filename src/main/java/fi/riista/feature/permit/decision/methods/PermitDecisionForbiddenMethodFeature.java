package fi.riista.feature.permit.decision.methods;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationService;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Component
public class PermitDecisionForbiddenMethodFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private PermitDecisionForbiddenMethodRepository permitDecisionForbiddenMethodRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionDerogationService permitDecisionDerogationService;

    @Transactional(readOnly = true)
    public PermitDecisionForbiddenMethodDTO getForbiddenMethods(final long decisionId,
                                                                final int gameSpeciesCode) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);

        final List<PermitDecisionForbiddenMethod> methodList =
                permitDecisionForbiddenMethodRepository.findByDecisionAndSpecies(decision, gameSpecies);

        final HashSet<ForbiddenMethodType> methodTypeSet =
                F.mapNonNullsToSet(methodList, PermitDecisionForbiddenMethod::getMethod);

        return new PermitDecisionForbiddenMethodDTO(methodTypeSet);
    }

    @Transactional
    public void updateForbiddenMethods(final long decisionId,
                                       final int gameSpeciesCode,
                                       final PermitDecisionForbiddenMethodDTO dto) {
        final PermitDecision decision = permitDecisionDerogationService.requireDecisionDerogationEditable(decisionId);

        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);

        final List<PermitDecisionForbiddenMethod> existing =
                permitDecisionForbiddenMethodRepository.findByDecisionAndSpecies(decision, gameSpecies);

        final List<PermitDecisionForbiddenMethod> toCreate = new LinkedList<>();
        final List<PermitDecisionForbiddenMethod> toDelete = new LinkedList<>();
        final Set<ForbiddenMethodType> existingTypes = new HashSet<>();

        for (PermitDecisionForbiddenMethod f : existing) {
            existingTypes.add(f.getMethod());

            if (!dto.getForbiddenMethodTypes().contains(f.getMethod())) {
                toDelete.add(f);
            }
        }

        for (ForbiddenMethodType methodType : dto.getForbiddenMethodTypes()) {
            if (!existingTypes.contains(methodType)) {
                final PermitDecisionForbiddenMethod f = new PermitDecisionForbiddenMethod();
                f.setPermitDecision(decision);
                f.setGameSpecies(gameSpecies);
                f.setMethod(methodType);
                toCreate.add(f);
            }
        }

        permitDecisionForbiddenMethodRepository.saveAll(toCreate);
        permitDecisionForbiddenMethodRepository.deleteAll(toDelete);
        permitDecisionSpeciesAmountRepository.setForbiddenMethodComplete(decision, gameSpecies);
    }

}
