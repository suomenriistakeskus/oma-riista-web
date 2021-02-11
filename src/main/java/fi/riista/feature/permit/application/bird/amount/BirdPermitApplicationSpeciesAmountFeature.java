package fi.riista.feature.permit.application.bird.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BirdPermitApplicationSpeciesAmountFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public List<BirdPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(BirdPermitApplicationSpeciesAmountDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesAmounts(final long applicationId,
                                   final List<BirdPermitApplicationSpeciesAmountDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater speciesUpdater = new Updater(existingSpecies, createCallback(application));
        speciesUpdater.processAll(dtoList);

        harvestPermitApplicationSpeciesAmountRepository.saveAll(speciesUpdater.getResultList());
        harvestPermitApplicationSpeciesAmountRepository.deleteAll(speciesUpdater.getMissing());
    }

    @Nonnull
    private UpdaterCallback createCallback(final HarvestPermitApplication application) {
        return new UpdaterCallback() {
            @Override
            public HarvestPermitApplicationSpeciesAmount create(final BirdPermitApplicationSpeciesAmountDTO dto) {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());

                return HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, dto.getAmount());
            }

            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final BirdPermitApplicationSpeciesAmountDTO dto) {
                entity.setSpecimenAmount(dto.getAmount());
            }

            @Override
            public int getSpeciesCode(final BirdPermitApplicationSpeciesAmountDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<BirdPermitApplicationSpeciesAmountDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<BirdPermitApplicationSpeciesAmountDTO> {
    }
}
