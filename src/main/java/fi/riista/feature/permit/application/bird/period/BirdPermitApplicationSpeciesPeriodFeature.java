package fi.riista.feature.permit.application.bird.period;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BirdPermitApplicationSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Transactional(readOnly = true)
    public BirdPermitApplicationSpeciesPeriodInformationDTO getPermitPeriodInformation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final BirdPermitApplication birdApplication =
                birdPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(birdApplication, "Bird application must be defined when setting species periods");

        final boolean limitlessPermitAllowed = birdApplication.isLimitlessPermitAllowed();
        final Integer validityYears = application.getValidityYears();

        final List<BirdPermitApplicationSpeciesPeriodDTO> amounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(BirdPermitApplicationSpeciesPeriodDTO::new)
                .collect(Collectors.toList());

        return new BirdPermitApplicationSpeciesPeriodInformationDTO(amounts, validityYears, limitlessPermitAllowed);
    }

    @Transactional
    public void saveSpeciesPeriods(final long applicationId,
                                   final BirdPermitApplicationSpeciesPeriodInformationDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final BirdPermitApplication birdApplication =
                birdPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(birdApplication, "Bird application must be defined when setting species periods");

        final int validityYears = Objects.requireNonNull(dto.getValidityYears());
        final boolean limitlessPermitAllowed = birdApplication.isLimitlessPermitAllowed();

        Preconditions.checkArgument(HarvestPermitApplicationSpeciesAmount.checkValidityYears(validityYears, limitlessPermitAllowed));

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(existingSpecies, createCallback(validityYears));
        updater.processAll(dto.getSpeciesPeriods());
        updater.assertNoSpeciesMissing();
    }

    @Nonnull
    private UpdaterCallback createCallback(final int validityYears) {
        return new UpdaterCallback() {
            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final BirdPermitApplicationSpeciesPeriodDTO dto) {
                entity.setBeginDate(dto.getBeginDate());
                entity.setEndDate(dto.getEndDate());
                entity.setValidityYears(validityYears);
                entity.setAdditionalPeriodInfo(dto.getAdditionalPeriodInfo());
            }

            @Override
            public int getSpeciesCode(final BirdPermitApplicationSpeciesPeriodDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<BirdPermitApplicationSpeciesPeriodDTO> {
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<BirdPermitApplicationSpeciesPeriodDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }
}
