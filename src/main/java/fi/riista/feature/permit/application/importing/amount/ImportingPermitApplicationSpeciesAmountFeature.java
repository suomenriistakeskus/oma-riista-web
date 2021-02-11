package fi.riista.feature.permit.application.importing.amount;

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

import static fi.riista.util.F.mapNullable;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Service
public class ImportingPermitApplicationSpeciesAmountFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Transactional(readOnly = true)
    public List<ImportingPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(ImportingPermitApplicationSpeciesAmountDTO::new)
                .collect(toList());
    }

    @Transactional
    public void saveSpeciesAmounts(final long applicationId,
                                   final List<ImportingPermitApplicationSpeciesAmountDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final ImportingPermitApplicationSpeciesAmountFeature.Updater speciesUpdater =
                new ImportingPermitApplicationSpeciesAmountFeature.Updater(existingSpecies, createCallback(application));
        speciesUpdater.processAll(dtoList);

        harvestPermitApplicationSpeciesAmountRepository.saveAll(speciesUpdater.getResultList());
        harvestPermitApplicationSpeciesAmountRepository.deleteAll(speciesUpdater.getMissing());
    }

    @Nonnull
    private ImportingPermitApplicationSpeciesAmountFeature.UpdaterCallback createCallback(final HarvestPermitApplication application) {
        return new ImportingPermitApplicationSpeciesAmountFeature.UpdaterCallback() {
            @Override
            public HarvestPermitApplicationSpeciesAmount create(final ImportingPermitApplicationSpeciesAmountDTO dto) {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());

                return HarvestPermitApplicationSpeciesAmount.createWithSpecimenOrEggs(application, gameSpecies,
                        dto.getSpecimenAmount(), dto.getEggAmount(), dto.getSubSpeciesName());
            }

            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final ImportingPermitApplicationSpeciesAmountDTO dto) {
                entity.setSpecimenAmount(mapNullable(dto.getSpecimenAmount(), Float::valueOf));
                entity.setEggAmount(dto.getEggAmount());
                entity.setSubSpeciesName(dto.getSubSpeciesName());
            }

            @Override
            public int getSpeciesCode(final ImportingPermitApplicationSpeciesAmountDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<ImportingPermitApplicationSpeciesAmountDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final ImportingPermitApplicationSpeciesAmountFeature.UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<ImportingPermitApplicationSpeciesAmountDTO> {
    }
}
