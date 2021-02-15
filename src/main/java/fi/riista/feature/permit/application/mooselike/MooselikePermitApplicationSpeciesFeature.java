package fi.riista.feature.permit.application.mooselike;

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
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class MooselikePermitApplicationSpeciesFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public List<MooselikePermitApplicationSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .map(MooselikePermitApplicationSpeciesAmountDTO::create)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesAmounts(
            final long applicationId, final List<MooselikePermitApplicationSpeciesAmountDTO> dtoList) {

        assertHasNoDuplicatesBySpecies(dtoList);

        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmountList =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(speciesAmountList, createCallback(application));
        updater.processAll(dtoList);

        if (!updater.getResultList().isEmpty()) {
            // Force change to application to avoid possible duplicates in amounts
            application.forceRevisionUpdate();
        }

        harvestPermitApplicationSpeciesAmountRepository.saveAll(updater.getResultList());
        harvestPermitApplicationSpeciesAmountRepository.deleteAll(updater.getMissing());
    }

    @Nonnull
    private UpdaterCallback createCallback(final HarvestPermitApplication application) {
        return new UpdaterCallback() {
            @Override
            public HarvestPermitApplicationSpeciesAmount create(final MooselikePermitApplicationSpeciesAmountDTO dto) {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());
                final HarvestPermitApplicationSpeciesAmount speciesAmount =
                        HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, dto.getAmount());
                speciesAmount.setMooselikeDescription(dto.getDescription());

                return speciesAmount;
            }

            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final MooselikePermitApplicationSpeciesAmountDTO dto) {
                entity.setSpecimenAmount(dto.getAmount());
                entity.setMooselikeDescription(dto.getDescription());
            }

            @Override
            public int getSpeciesCode(final MooselikePermitApplicationSpeciesAmountDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<MooselikePermitApplicationSpeciesAmountDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<MooselikePermitApplicationSpeciesAmountDTO> {
    }

    private static void assertHasNoDuplicatesBySpecies(final List<MooselikePermitApplicationSpeciesAmountDTO> dtoList) {
        final long speciesCount =
                dtoList.stream()
                        .map(MooselikePermitApplicationSpeciesAmountDTO::getGameSpeciesCode)
                        .distinct()
                        .count();
        checkArgument(speciesCount == dtoList.size(), "List contains duplicates by species");
    }
}
