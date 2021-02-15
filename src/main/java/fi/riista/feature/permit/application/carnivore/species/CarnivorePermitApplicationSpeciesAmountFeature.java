package fi.riista.feature.permit.application.carnivore.species;

import com.google.common.collect.Range;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkState;

@Component
public class CarnivorePermitApplicationSpeciesAmountFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public CarnivorePermitApplicationSpeciesAmountDTO getSpeciesAmount(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        CarnivorePermitSpecies.assertCategory(application.getHarvestPermitCategory());

        return harvestPermitApplicationSpeciesAmountRepository
                .findAtMostOneByHarvestPermitApplication(application)
                .map(spa -> CarnivorePermitApplicationSpeciesAmountDTO.create(application, spa))
                .orElseGet(() -> CarnivorePermitApplicationSpeciesAmountDTO.create(application));
    }

    @Transactional
    public void saveSpeciesAmount(final long applicationId, final CarnivorePermitApplicationSpeciesAmountDTO dto) {

        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);
        CarnivorePermitSpecies.assertCategory(application.getHarvestPermitCategory());
        CarnivorePermitSpecies.assertSpecies(application.getHarvestPermitCategory(), dto.getGameSpeciesCode());
        CarnivorePermitSpecies.assertValidPeriod(application, Range.closed(dto.getBegin(), dto.getEnd()));
        final HarvestPermitApplicationSpeciesAmount speciesAmount = harvestPermitApplicationSpeciesAmountRepository
                .findAtMostOneByHarvestPermitApplication(application)
                .map(spa -> {
                    checkState(dto.getGameSpeciesCode() == spa.getGameSpecies().getOfficialCode());
                    spa.setSpecimenAmount(dto.getAmount());
                    return spa;
                })
                .orElseGet(() -> {
                    final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
                    return HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, dto.getAmount());
                });

        speciesAmount.setBeginDate(dto.getBegin());
        speciesAmount.setEndDate(dto.getEnd());
        harvestPermitApplicationSpeciesAmountRepository.save(speciesAmount);
    }

}
