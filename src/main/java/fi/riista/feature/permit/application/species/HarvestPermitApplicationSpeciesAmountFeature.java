package fi.riista.feature.permit.application.species;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationLockedCondition;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HarvestPermitApplicationSpeciesAmountFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationLockedCondition harvestPermitApplicationLockedCondition;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        return application.getSpeciesAmounts().stream()
                .map(HarvestPermitApplicationSpeciesAmountDTO::create)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<HarvestPermitApplicationSpeciesAmountDTO> saveSpeciesAmounts(final long applicationId,
                                                                             final List<HarvestPermitApplicationSpeciesAmountDTO> dto) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);
        harvestPermitApplicationLockedCondition.assertCanUpdate(application);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = dto.stream().map(d -> {
            final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(d.getGameSpeciesCode());
            final HarvestPermitApplicationSpeciesAmount spa =
                    new HarvestPermitApplicationSpeciesAmount(application, gameSpecies, d.getAmount());
            spa.setDescription(d.getDescription());
            return spa;
        }).collect(Collectors.toList());

        harvestPermitApplicationSpeciesAmountRepository.deleteByHarvestPermitApplication(application);
        harvestPermitApplicationSpeciesAmountRepository.save(speciesAmounts);

        return speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmountDTO::create)
                .collect(Collectors.toList());
    }
}
