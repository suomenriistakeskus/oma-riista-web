package fi.riista.feature.permit.decision.species;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PermitDecisionSpeciesAmountFeature {
    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<PermitDecisionSpeciesAmountDTO> getSpeciesAmounts(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);

        final Function<GameSpecies, Float> applicationAmountMapping = getApplicationSpeciesAmounts(decision);

        return decision.getSpeciesAmounts().stream()
                .map(spa -> PermitDecisionSpeciesAmountDTO.create(spa, applicationAmountMapping.apply(spa.getGameSpecies())))
                .collect(Collectors.toList());
    }

    private static Function<GameSpecies, Float> getApplicationSpeciesAmounts(final PermitDecision decision) {
        if (decision.getApplication() == null) {
            return g -> 0f;
        }

        return species -> decision.getApplication().getSpeciesAmounts().stream()
                .filter(spa -> spa.getGameSpecies().equals(species))
                .map(HarvestPermitApplicationSpeciesAmount::getAmount).findAny()
                .orElse(0f);
    }

    @Transactional
    public void saveSpeciesAmounts(final long decisionId, final PermitDecisionSpeciesAmountDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        if (dto.getRestrictionAmount() == null || dto.getRestrictionType() == null) {
            dto.setRestrictionAmount(null);
            dto.setRestrictionType(null);
        }

        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        final float applicationSpeciesAmount = getApplicationSpeciesAmounts(decision).apply(gameSpecies);

        if (applicationSpeciesAmount > 0 && dto.getAmount() > applicationSpeciesAmount) {
            throw new IllegalArgumentException(String.format("Amount %.1f exceeds value in application %.1f",
                    dto.getAmount(), applicationSpeciesAmount));
        }

        final Optional<PermitDecisionSpeciesAmount> existingSpeciesAmount = decision.getSpeciesAmounts().stream()
                .filter(spa -> spa.getGameSpecies().equals(gameSpecies)).findAny();

        if (existingSpeciesAmount.isPresent()) {
            final PermitDecisionSpeciesAmount spa = existingSpeciesAmount.get();
            spa.copyDatesFrom(dto);
            spa.setAmount(dto.getAmount());
            spa.setRestrictionType(dto.getRestrictionType());
            spa.setRestrictionAmount(dto.getRestrictionAmount());

        } else {
            final PermitDecisionSpeciesAmount spa = new PermitDecisionSpeciesAmount();
            spa.setPermitDecision(decision);
            spa.setGameSpecies(gameSpecies);
            spa.copyDatesFrom(dto);
            spa.setAmount(dto.getAmount());
            spa.setRestrictionType(dto.getRestrictionType());
            spa.setRestrictionAmount(dto.getRestrictionAmount());

            decision.getSpeciesAmounts().add(permitDecisionSpeciesAmountRepository.save(spa));
        }

        decision.updateGrantStatus();
        updateDecisionText(decision);
    }

    @Transactional
    public void deleteSpeciesAmounts(final long decisionId,
                                     final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        final Iterator<PermitDecisionSpeciesAmount> iterator = decision.getSpeciesAmounts().iterator();

        while (iterator.hasNext()) {
            final PermitDecisionSpeciesAmount speciesAmount = iterator.next();

            if (speciesAmount.getId() == id) {
                permitDecisionSpeciesAmountRepository.delete(speciesAmount.getId());
                iterator.remove();
            }
        }

        decision.updateGrantStatus();
        updateDecisionText(decision);
    }

    private void updateDecisionText(final PermitDecision decision) {
        final PermitDecisionDocument document = decision.getDocument();

        document.setDecision(permitDecisionTextService.generateDecision(decision));
        document.setRestriction(permitDecisionTextService.generateRestriction(decision));
    }
}
