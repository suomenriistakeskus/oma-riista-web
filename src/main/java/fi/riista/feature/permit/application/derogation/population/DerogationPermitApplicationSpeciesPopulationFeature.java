package fi.riista.feature.permit.application.derogation.population;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DerogationPermitApplicationSpeciesPopulationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public List<DerogationPermitApplicationSpeciesPopulationDTO> getSpeciesPopulation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(DerogationPermitApplicationSpeciesPopulationDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesPopulation(final long applicationId,
                                      final List<DerogationPermitApplicationSpeciesPopulationDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(existingSpecies, new UpdaterCallback());
        updater.processAll(dtoList);
        updater.assertNoSpeciesMissing();
    }

    static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<DerogationPermitApplicationSpeciesPopulationDTO> {
        @Override
        public void update(final HarvestPermitApplicationSpeciesAmount entity,
                           final DerogationPermitApplicationSpeciesPopulationDTO dto) {
            entity.setPopulationAmount(dto.getPopulationAmount());
            entity.setPopulationDescription(dto.getPopulationDescription());
        }

        @Override
        public int getSpeciesCode(final DerogationPermitApplicationSpeciesPopulationDTO dto) {
            return dto.getGameSpeciesCode();
        }
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<DerogationPermitApplicationSpeciesPopulationDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }
}
