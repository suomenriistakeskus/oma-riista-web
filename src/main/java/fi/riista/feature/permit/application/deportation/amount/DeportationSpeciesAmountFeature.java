package fi.riista.feature.permit.application.deportation.amount;

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
public class DeportationSpeciesAmountFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public DeportationSpeciesAmountDTO getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final DeportationSpeciesAmountDTO deportationSpeciesAmountDTO = new DeportationSpeciesAmountDTO();
        final List<HarvestPermitApplicationSpeciesAmount> speciesAmountList = application.getSpeciesAmounts();
        if (!speciesAmountList.isEmpty()) {
            final HarvestPermitApplicationSpeciesAmount speciesAmount = speciesAmountList.get(0);
            deportationSpeciesAmountDTO.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());
            deportationSpeciesAmountDTO.setAmount(speciesAmount.getSpecimenAmount().intValue());
        }

        return deportationSpeciesAmountDTO;
    }

    @Transactional
    public void saveSpeciesAmounts(final long applicationId,
                                   final DeportationSpeciesAmountDTO deportationSpeciesAmountDTO) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        if (existingSpecies.isEmpty()) {
            createSpeciesAmount(application, deportationSpeciesAmountDTO);
        } else {
            final HarvestPermitApplicationSpeciesAmount speciesAmount = existingSpecies.get(0);
            if (speciesAmount.getGameSpecies().getOfficialCode() == deportationSpeciesAmountDTO.getGameSpeciesCode()) {
                speciesAmount.setSpecimenAmount(deportationSpeciesAmountDTO.getAmount().floatValue());
            } else {
                harvestPermitApplicationSpeciesAmountRepository.delete(speciesAmount);
                createSpeciesAmount(application, deportationSpeciesAmountDTO);
            }
        }
    }

    private void createSpeciesAmount(final HarvestPermitApplication application,
                                     final DeportationSpeciesAmountDTO deportationSpeciesAmountDTO) {
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(deportationSpeciesAmountDTO.getGameSpeciesCode());
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, deportationSpeciesAmountDTO.getAmount());
        harvestPermitApplicationSpeciesAmountRepository.save(speciesAmount);
    }
}
