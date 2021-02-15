package fi.riista.feature.permit.application.nestremoval.period;

import com.google.common.base.Preconditions;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplication;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NestRemovalPermitApplicationSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Transactional(readOnly = true)
    public NestRemovalPermitApplicationSpeciesPeriodInformationDTO getPermitPeriodInformation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final NestRemovalPermitApplication nestRemovalApplication =
                nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(nestRemovalApplication, "Nest removal application must be defined when setting species periods");

        final List<NestRemovalPermitApplicationSpeciesPeriodDTO> amounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(NestRemovalPermitApplicationSpeciesPeriodDTO::new)
                .collect(Collectors.toList());

        return new NestRemovalPermitApplicationSpeciesPeriodInformationDTO(amounts);
    }

    @Transactional
    public void saveSpeciesPeriods(final long applicationId,
                                   final NestRemovalPermitApplicationSpeciesPeriodInformationDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final NestRemovalPermitApplication nestRemovalApplication =
                nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(nestRemovalApplication, "Nest removal application must be defined when setting species periods");

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final NestRemovalPermitApplicationSpeciesPeriodFeature.Updater updater =
                new NestRemovalPermitApplicationSpeciesPeriodFeature.Updater(existingSpecies, createCallback());
        updater.processAll(dto.getSpeciesPeriods());
        updater.assertNoSpeciesMissing();
    }

    @Nonnull
    private static NestRemovalPermitApplicationSpeciesPeriodFeature.UpdaterCallback createCallback() {
        return new NestRemovalPermitApplicationSpeciesPeriodFeature.UpdaterCallback() {
            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final NestRemovalPermitApplicationSpeciesPeriodDTO dto) {
                entity.setBeginDate(dto.getBeginDate());
                entity.setEndDate(dto.getEndDate());
            }

            @Override
            public int getSpeciesCode(final NestRemovalPermitApplicationSpeciesPeriodDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<NestRemovalPermitApplicationSpeciesPeriodDTO> {
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<NestRemovalPermitApplicationSpeciesPeriodDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final NestRemovalPermitApplicationSpeciesPeriodFeature.UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

}
