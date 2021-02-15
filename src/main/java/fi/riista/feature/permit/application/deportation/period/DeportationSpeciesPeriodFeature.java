package fi.riista.feature.permit.application.deportation.period;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DeportationSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Transactional(readOnly = true)
    public DeportationSpeciesPeriodDTO getSpeciesPeriod(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final DeportationPermitApplication deportationPermitApplication =
                deportationPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(deportationPermitApplication,
                "Deportation application must be defined when setting species periods");

        return new DeportationSpeciesPeriodDTO(application.getSpeciesAmounts().get(0));

    }

    @Transactional
    public void saveSpeciesPeriod(final long applicationId,
                                  final DeportationSpeciesPeriodDTO deportationSpeciesPeriodDTO) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final DeportationPermitApplication deportationPermitApplication =
                deportationPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(deportationPermitApplication,
                "Deportation application must be defined when setting species periods");

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        Preconditions.checkState(existingSpecies.size() == 1, "Species must be set for periods");

        final HarvestPermitApplicationSpeciesAmount speciesAmount = existingSpecies.get(0);
        Preconditions.checkState(speciesAmount.getGameSpecies().getOfficialCode() == deportationSpeciesPeriodDTO.getGameSpeciesCode(),
                "Period must be for existing species");
        speciesAmount.setBeginDate(deportationSpeciesPeriodDTO.getBeginDate());
        speciesAmount.setEndDate(deportationSpeciesPeriodDTO.getEndDate());
        speciesAmount.setAdditionalPeriodInfo(deportationSpeciesPeriodDTO.getAdditionalPeriodInfo());
        speciesAmount.setValidityYears(1);
    }
}
