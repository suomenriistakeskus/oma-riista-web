package fi.riista.feature.permit.application.mammal.amount;

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
public class MammalPermitApplicationSpeciesAmountFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public List<MammalPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(MammalPermitApplicationSpeciesAmountDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesAmounts(final long applicationId,
                                   final List<MammalPermitApplicationSpeciesAmountDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        validateSpecies(dtoList);

        final Updater speciesUpdater = new Updater(existingSpecies, createCallback(application));
        speciesUpdater.processAll(dtoList);

        harvestPermitApplicationSpeciesAmountRepository.save(speciesUpdater.getResultList());
        harvestPermitApplicationSpeciesAmountRepository.delete(speciesUpdater.getMissing());
    }

    private void validateSpecies(final List<MammalPermitApplicationSpeciesAmountDTO> dtoList) {
        if (dtoList.size() > 1) {
            dtoList.stream()
                    .map(MammalPermitApplicationSpeciesAmountDTO::getGameSpeciesCode)
                    .filter(code -> GameSpecies.isLargeCarnivore(code) || code == GameSpecies.OFFICIAL_CODE_OTTER)
                    .findAny()
                    .ifPresent(code -> {
                                throw new IllegalArgumentException("Contains species which need to be applied " +
                                        "separately:" + code);
                            }
                    );
        }
    }


    @Nonnull
    private UpdaterCallback createCallback(final HarvestPermitApplication application) {
        return new UpdaterCallback() {
            @Override
            public HarvestPermitApplicationSpeciesAmount create(final MammalPermitApplicationSpeciesAmountDTO dto) {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());

                return new HarvestPermitApplicationSpeciesAmount(application, gameSpecies, dto.getAmount());
            }

            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final MammalPermitApplicationSpeciesAmountDTO dto) {
                entity.setAmount(dto.getAmount());
            }

            @Override
            public int getSpeciesCode(final MammalPermitApplicationSpeciesAmountDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<MammalPermitApplicationSpeciesAmountDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<MammalPermitApplicationSpeciesAmountDTO> {
    }
}
