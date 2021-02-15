package fi.riista.feature.permit.application.nestremoval.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NestRemovalPermitApplicationSpeciesAmountFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Transactional(readOnly = true)
    public List<NestRemovalPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(NestRemovalPermitApplicationSpeciesAmountDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesAmounts(final long applicationId,
                                   final List<NestRemovalPermitApplicationSpeciesAmountDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        validateSpecies(dtoList);

        final NestRemovalPermitApplicationSpeciesAmountFeature.Updater speciesUpdater =
                new NestRemovalPermitApplicationSpeciesAmountFeature.Updater(existingSpecies, createCallback(application));
        speciesUpdater.processAll(dtoList);

        harvestPermitApplicationSpeciesAmountRepository.saveAll(speciesUpdater.getResultList());
        harvestPermitApplicationSpeciesAmountRepository.deleteAll(speciesUpdater.getMissing());
    }

    private static void validateSpecies(final List<NestRemovalPermitApplicationSpeciesAmountDTO> dtoList) {
        dtoList.forEach(dto -> {
            final int speciesCode = dto.getGameSpeciesCode();
            if (!GameSpecies.isNestRemovalPermitSpecies(speciesCode)) {
                throw new IllegalArgumentException("Not a nest removal permit application species:" + speciesCode);
            }
        });
    }

    @Nonnull
    private NestRemovalPermitApplicationSpeciesAmountFeature.UpdaterCallback createCallback(final HarvestPermitApplication application) {
        return new NestRemovalPermitApplicationSpeciesAmountFeature.UpdaterCallback() {
            @Override
            public HarvestPermitApplicationSpeciesAmount create(final NestRemovalPermitApplicationSpeciesAmountDTO dto) {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());

                return HarvestPermitApplicationSpeciesAmount.createForNestRemoval(application, gameSpecies,
                        dto.getNestAmount(), dto.getEggAmount(), dto.getConstructionAmount());
            }

            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final NestRemovalPermitApplicationSpeciesAmountDTO dto) {
                entity.setNestAmount(dto.getNestAmount());
                entity.setEggAmount(dto.getEggAmount());
                entity.setConstructionAmount(dto.getConstructionAmount());
            }

            @Override
            public int getSpeciesCode(final NestRemovalPermitApplicationSpeciesAmountDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<NestRemovalPermitApplicationSpeciesAmountDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final NestRemovalPermitApplicationSpeciesAmountFeature.UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<NestRemovalPermitApplicationSpeciesAmountDTO> {
    }
}
