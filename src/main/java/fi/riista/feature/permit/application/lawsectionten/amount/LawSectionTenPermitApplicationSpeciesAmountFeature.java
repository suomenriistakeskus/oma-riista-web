package fi.riista.feature.permit.application.lawsectionten.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LawSectionTenPermitApplicationSpeciesAmountFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Transactional(readOnly = true)
    public LawSectionTenPermitApplicationSpeciesAmountDTO getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        final LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmountDTO = application.getSpeciesAmounts().stream()
                .map(LawSectionTenPermitApplicationSpeciesAmountDTO::new)
                .findFirst()
                .orElse(null);

        return speciesAmountDTO;
    }

    @Transactional
    public void saveSpeciesAmounts(final long applicationId,
                                   final LawSectionTenPermitApplicationSpeciesAmountDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpeciesList =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
        if (existingSpeciesList != null && !existingSpeciesList.isEmpty()) {
            final HarvestPermitApplicationSpeciesAmount existingSpecies = existingSpeciesList.get(0);
            final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
            existingSpecies.setGameSpecies(gameSpecies);
            existingSpecies.setSpecimenAmount(dto.getAmount());
        } else {
            createSpeciesAmount(application, dto);
        }
    }

    private void createSpeciesAmount(final HarvestPermitApplication application, final LawSectionTenPermitApplicationSpeciesAmountDTO dto) {
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, dto.getAmount());
        harvestPermitApplicationSpeciesAmountRepository.save(speciesAmount);
    }

}
