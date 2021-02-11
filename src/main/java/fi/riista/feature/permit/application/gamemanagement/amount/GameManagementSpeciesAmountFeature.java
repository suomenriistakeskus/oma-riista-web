package fi.riista.feature.permit.application.gamemanagement.amount;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GameManagementSpeciesAmountFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public GameManagementSpeciesAmountDTO getSpeciesAmount(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final GameManagementSpeciesAmountDTO speciesAmountDTO = new GameManagementSpeciesAmountDTO();
        final List<HarvestPermitApplicationSpeciesAmount> speciesAmountList = application.getSpeciesAmounts();
        if (!speciesAmountList.isEmpty()) {
            final HarvestPermitApplicationSpeciesAmount speciesAmount = speciesAmountList.get(0);
            speciesAmountDTO.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());
            speciesAmountDTO.setSubSpeciesName(speciesAmount.getSubSpeciesName());
            speciesAmountDTO.setSpecimenAmount(F.mapNullable(speciesAmount.getSpecimenAmount(), Float::intValue));
            speciesAmountDTO.setEggAmount(speciesAmount.getEggAmount());
        }

        return speciesAmountDTO;
    }

    @Transactional
    public void saveSpeciesAmount(final long applicationId,
                                  final GameManagementSpeciesAmountDTO speciesAmountDTO) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        if (existingSpecies.isEmpty()) {
            createSpeciesAmount(application, speciesAmountDTO);
        } else {
            Preconditions.checkState(existingSpecies.size() == 1,
                    "Only one species allowed for game management application");

            final HarvestPermitApplicationSpeciesAmount speciesAmount = existingSpecies.get(0);
            if (speciesAmount.getGameSpecies().getOfficialCode() == speciesAmountDTO.getGameSpeciesCode()) {
                speciesAmount.setSubSpeciesName(speciesAmountDTO.getSubSpeciesName());
                speciesAmount.setSpecimenAmount(F.mapNullable(speciesAmountDTO.getSpecimenAmount(), Integer::floatValue));
                speciesAmount.setEggAmount(speciesAmountDTO.getEggAmount());
            } else {
                harvestPermitApplicationSpeciesAmountRepository.delete(speciesAmount);
                createSpeciesAmount(application, speciesAmountDTO);
            }
        }
    }

    private void createSpeciesAmount(final HarvestPermitApplication application,
                                     final GameManagementSpeciesAmountDTO speciesAmountDTO) {
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(speciesAmountDTO.getGameSpeciesCode());
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                HarvestPermitApplicationSpeciesAmount.createWithSpecimenOrEggs(
                        application,
                        gameSpecies,
                        speciesAmountDTO.getSpecimenAmount(),
                        speciesAmountDTO.getEggAmount(),
                        speciesAmountDTO.getSubSpeciesName());
        harvestPermitApplicationSpeciesAmountRepository.save(speciesAmount);
    }
}
