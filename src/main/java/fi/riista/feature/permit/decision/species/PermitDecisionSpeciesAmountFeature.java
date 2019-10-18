package fi.riista.feature.permit.decision.species;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.PermitDecisionGrantStatusService;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class PermitDecisionSpeciesAmountFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private PermitDecisionGrantStatusService permitDecisionGrantStatusService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository applicationSpeciesAmountRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<PermitDecisionSpeciesAmountDTO> getSpeciesAmounts(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        final HarvestPermitApplication application = decision.getApplication();
        final Function<Integer, Float> applicationAmountMapping = getApplicationAmountMapping(application);

        return F.mapNonNullsToList(permitDecisionSpeciesAmountRepository.findByPermitDecision(decision), spa -> {
            final int speciesCode = spa.getGameSpecies().getOfficialCode();
            final float applicationAmount = applicationAmountMapping.apply(speciesCode);

            return PermitDecisionSpeciesAmountDTO.create(spa, application, applicationAmount);
        });
    }

    @Transactional
    public void saveSpeciesAmounts(final long decisionId, final List<PermitDecisionSpeciesAmountDTO> dtoList) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());
        final HarvestPermitApplication application = decision.getApplication();

        final Function<PermitDecisionSpeciesAmountDTO, PermitDecisionSpeciesAmount> dtoToEntity =
                createEntityLookup(decision);

        // VALIDATE
        final Function<Integer, Float> applicationAmountMapping = getApplicationAmountMapping(application);

        dtoList.forEach(dto -> {
            final PermitDecisionSpeciesAmount entity = dtoToEntity.apply(dto);
            final float applicationAmount = applicationAmountMapping.apply(dto.getGameSpeciesCode());
            validateSpeciesAmount(entity, dto, applicationAmount);
        });

        // UPDATE
        dtoList.forEach(dto -> updateSpeciesAmount(dtoToEntity.apply(dto), dto));

        permitDecisionGrantStatusService.updateGrantStatus(decision);

        final PermitDecisionDocument document = decision.getDocument();

        document.setDecision(permitDecisionTextService.generateDecision(decision));
        document.setRestriction(permitDecisionTextService.generateRestriction(decision));
    }

    private Function<PermitDecisionSpeciesAmountDTO, PermitDecisionSpeciesAmount> createEntityLookup(
            final PermitDecision permitDecision) {
        final Map<Long, PermitDecisionSpeciesAmount> entityIndex = F.indexById(
                permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision));

        return dto -> Optional
                .ofNullable(dto.getId())
                .map(entityIndex::get)
                .orElseThrow(() -> speciesAmountNotFound(dto));
    }

    private static IllegalArgumentException speciesAmountNotFound(final PermitDecisionSpeciesAmountDTO dto) {
        return new IllegalArgumentException(String.format("Species amount id=%d not found", dto.getId()));
    }

    private Function<Integer, Float> getApplicationAmountMapping(final HarvestPermitApplication application) {
        final Map<Integer, Float> mapping = applicationSpeciesAmountRepository
                .findByHarvestPermitApplication(application).stream()
                .collect(toMap(a -> a.getGameSpecies().getOfficialCode(), HarvestPermitApplicationSpeciesAmount::getAmount));

        return speciesCode -> mapping.getOrDefault(speciesCode, 0f);
    }

    private static void validateSpeciesAmount(final @Nonnull PermitDecisionSpeciesAmount spa,
                                              final @Nonnull PermitDecisionSpeciesAmountDTO dto,
                                              final float applicationAmount) {
        if (dto.getAmount() > applicationAmount) {
            throw new IllegalArgumentException(String.format("Amount %.1f exceeds value in application %.1f",
                    dto.getAmount(), applicationAmount));
        }

        PermitDecisionSpeciesAmountDateRestriction.create(spa).assertValid(dto);
    }

    private static void updateSpeciesAmount(final @Nonnull PermitDecisionSpeciesAmount entity,
                                            final @Nonnull PermitDecisionSpeciesAmountDTO dto) {
        entity.copyDatesFrom(dto);
        entity.setAmount(dto.getAmount());
        entity.setAmountComplete(true);

        if (dto.getRestrictionAmount() != null && dto.getRestrictionType() != null) {
            entity.setRestrictionType(dto.getRestrictionType());
            entity.setRestrictionAmount(dto.getRestrictionAmount());
        } else {
            entity.setRestrictionType(null);
            entity.setRestrictionAmount(null);
        }
    }
}
