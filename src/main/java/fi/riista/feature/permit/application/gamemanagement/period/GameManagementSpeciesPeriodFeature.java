package fi.riista.feature.permit.application.gamemanagement.period;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GameManagementSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Transactional(readOnly = true)
    public GameManagementSpeciesPeriodDTO getSpeciesPeriod(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(gameManagementPermitApplication,
                "Game management application must be defined when setting species periods");

        return new GameManagementSpeciesPeriodDTO(application.getSpeciesAmounts().get(0));
    }

    @Transactional
    public void saveSpeciesPeriod(final long applicationId,
                                  final GameManagementSpeciesPeriodDTO speciesPeriodDTO) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(gameManagementPermitApplication,
                "Game management application must be defined when setting species periods");

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        Preconditions.checkState(existingSpecies.size() == 1, "Species must be set for periods");

        final HarvestPermitApplicationSpeciesAmount speciesAmount = existingSpecies.get(0);
        Preconditions.checkState(speciesAmount.getGameSpecies().getOfficialCode() == speciesPeriodDTO.getGameSpeciesCode(),
                "Period must be for existing species");
        speciesAmount.setBeginDate(speciesPeriodDTO.getBeginDate());
        speciesAmount.setEndDate(speciesPeriodDTO.getEndDate());
        speciesAmount.setAdditionalPeriodInfo(speciesPeriodDTO.getAdditionalPeriodInfo());
        speciesAmount.setValidityYears(speciesPeriodDTO.getValidityYears());
    }
}
