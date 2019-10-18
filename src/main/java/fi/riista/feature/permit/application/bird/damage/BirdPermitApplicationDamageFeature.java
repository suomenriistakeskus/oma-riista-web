package fi.riista.feature.permit.application.bird.damage;

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
public class BirdPermitApplicationDamageFeature {

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Transactional(readOnly = true)
    public List<BirdPermitApplicationDamageDTO> getSpeciesDamage(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(BirdPermitApplicationDamageDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesDamage(final long applicationId,
                                  final List<BirdPermitApplicationDamageDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(existingSpecies, new UpdaterCallback());
        updater.processAll(dtoList);
        updater.assertNoSpeciesMissing();
    }

    static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<BirdPermitApplicationDamageDTO> {
        @Override
        public void update(final HarvestPermitApplicationSpeciesAmount entity,
                           final BirdPermitApplicationDamageDTO dto) {
            entity.setCausedDamageAmount(dto.getCausedDamageAmount());
            entity.setCausedDamageDescription(dto.getCausedDamageDescription());
            entity.setEvictionMeasureDescription(dto.getEvictionMeasureDescription());
            entity.setEvictionMeasureEffect(dto.getEvictionMeasureEffect());
        }

        @Override
        public int getSpeciesCode(final BirdPermitApplicationDamageDTO dto) {
            return dto.getGameSpeciesCode();
        }
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<BirdPermitApplicationDamageDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

}
