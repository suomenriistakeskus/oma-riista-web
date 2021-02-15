package fi.riista.feature.permit.application.importing.period;

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
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.IMPORTING;

@Component
public class ImportingPermitApplicationSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public ImportingPermitApplicationSpeciesPeriodInformationDTO getPermitPeriodInformation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        checkArgument(application.getHarvestPermitCategory() == IMPORTING,
                "Category must be importing.");

        final Integer validityYears = application.getValidityYears();

        final List<ImportingPermitApplicationSpeciesPeriodDTO> amounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(ImportingPermitApplicationSpeciesPeriodDTO::new)
                .collect(Collectors.toList());

        return new ImportingPermitApplicationSpeciesPeriodInformationDTO(amounts, validityYears);
    }

    @Transactional
    public void saveSpeciesPeriods(final long applicationId,
                                   final ImportingPermitApplicationSpeciesPeriodInformationDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);
        checkArgument(application.getHarvestPermitCategory() == IMPORTING,
                "Category must be importing.");

        final int validityYears = Objects.requireNonNull(dto.getValidityYears());

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(existingSpecies, createCallback(validityYears));
        updater.processAll(dto.getSpeciesPeriods());
        updater.assertNoSpeciesMissing();
    }

    @Nonnull
    private static UpdaterCallback createCallback(final int validityYears) {
        return new UpdaterCallback() {
            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final ImportingPermitApplicationSpeciesPeriodDTO dto) {
                entity.setValidityYears(validityYears);
                entity.setBeginDate(dto.getBeginDate());
                entity.setEndDate(dto.getEndDate());
                entity.setAdditionalPeriodInfo(dto.getAdditionalPeriodInfo());
            }

            @Override
            public int getSpeciesCode(final ImportingPermitApplicationSpeciesPeriodDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<ImportingPermitApplicationSpeciesPeriodDTO> {
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<ImportingPermitApplicationSpeciesPeriodDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }
}
