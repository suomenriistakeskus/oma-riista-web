package fi.riista.feature.permit.application.research.period;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ResearchSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Transactional(readOnly = true)
    public ResearchSpeciesPeriodInformationDTO getSpeciesPeriods(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(researchPermitApplication,
                "Research application must be defined when setting species periods");

        final Integer validityYears = application.getValidityYears();

        final List<ResearchSpeciesPeriodDTO> periods = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(ResearchSpeciesPeriodDTO::new)
                .collect(Collectors.toList());

        return new ResearchSpeciesPeriodInformationDTO(periods, validityYears);
    }

    @Transactional
    public void saveSpeciesPeriods(final long applicationId,
                                   final ResearchSpeciesPeriodInformationDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(researchPermitApplication,
                "Research application must be defined when setting species periods");

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
                               final ResearchSpeciesPeriodDTO dto) {
                entity.setBeginDate(dto.getBeginDate());
                entity.setEndDate(dto.getEndDate());
                entity.setValidityYears(validityYears);
                entity.setAdditionalPeriodInfo(dto.getAdditionalPeriodInfo());
            }

            @Override
            public int getSpeciesCode(final ResearchSpeciesPeriodDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<ResearchSpeciesPeriodDTO> {
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<ResearchSpeciesPeriodDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }}
