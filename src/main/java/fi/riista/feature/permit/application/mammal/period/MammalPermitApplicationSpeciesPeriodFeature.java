package fi.riista.feature.permit.application.mammal.period;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MammalPermitApplicationSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Transactional(readOnly = true)
    public MammalPermitApplicationSpeciesPeriodInformationDTO getPermitPeriodInformation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final MammalPermitApplication mammalApplication =
                mammalPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(mammalApplication, "Mammal application must be defined when setting species " +
                "periods");

        final Integer validityYears = application.getValidityYears();

        final List<MammalPermitApplicationSpeciesPeriodDTO> amounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(MammalPermitApplicationSpeciesPeriodDTO::new)
                .collect(Collectors.toList());

        return new MammalPermitApplicationSpeciesPeriodInformationDTO(
                amounts,
                validityYears,
                mammalApplication.getExtendedPeriodGrounds(),
                mammalApplication.getExtendedPeriodGroundsDescription(),
                mammalApplication.getProtectedAreaName());
    }

    @Transactional
    public void saveSpeciesPeriods(final long applicationId,
                                   final MammalPermitApplicationSpeciesPeriodInformationDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final MammalPermitApplication mammalApplication =
                mammalPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(mammalApplication, "Mammal application must be defined when setting species " +
                "periods");

        final int validityYears = Objects.requireNonNull(dto.getValidityYears());

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        mammalApplication.setExtendedPeriodGrounds(dto.getExtendedPeriodGrounds());
        mammalApplication.setExtendedPeriodGroundsDescription(dto.getExtendedPeriodGroundsDescription());
        mammalApplication.setProtectedAreaName(dto.getProtectedAreaName());

        final Updater updater = new Updater(existingSpecies, createCallback(validityYears));
        updater.processAll(dto.getSpeciesPeriods());
        updater.assertNoSpeciesMissing();
    }

    @Nonnull
    private static UpdaterCallback createCallback(final int validityYears) {
        return new UpdaterCallback() {
            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final MammalPermitApplicationSpeciesPeriodDTO dto) {
                entity.setBeginDate(dto.getBeginDate());
                entity.setEndDate(dto.getEndDate());
                entity.setValidityYears(validityYears);
                entity.setAdditionalPeriodInfo(dto.getAdditionalPeriodInfo());
            }

            @Override
            public int getSpeciesCode(final MammalPermitApplicationSpeciesPeriodDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<MammalPermitApplicationSpeciesPeriodDTO> {
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<MammalPermitApplicationSpeciesPeriodDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }
}
