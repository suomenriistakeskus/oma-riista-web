package fi.riista.feature.permit.application.bird.forbidden;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationService;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BirdPermitApplicationForbiddenMethodsFeature {

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private BirdPermitApplicationService birdPermitApplicationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public DerogationPermitApplicationForbiddenMethodsDTO getCurrentMethodInfo(final long applicationId) {
        final BirdPermitApplication birdApplication =
                birdPermitApplicationService.findForRead(applicationId);

        if (birdApplication.getForbiddenMethods() == null) {
            return new DerogationPermitApplicationForbiddenMethodsDTO();
        }

        final List<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> justificationList = birdApplication
                .getHarvestPermitApplication().getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new)
                .collect(Collectors.toList());

        return DerogationPermitApplicationForbiddenMethodsDTO.createFrom(birdApplication.getForbiddenMethods(),
                justificationList);
    }

    @Transactional
    public void updateMethodInfo(final long applicationId,
                                 final @NotNull DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
        final BirdPermitApplication birdPermitApplication = birdPermitApplicationService.findOrCreateForUpdate(applicationId);
        final HarvestPermitApplication application = birdPermitApplication.getHarvestPermitApplication();
        birdPermitApplication.setForbiddenMethods(forbiddenMethods.toEntity());
        birdPermitApplicationRepository.save(birdPermitApplication);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(existingSpecies, new UpdaterCallback());
        updater.processAll(forbiddenMethods.getSpeciesJustifications());
        updater.assertNoSpeciesMissing();
    }

    static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> {
        @Override
        public void update(final HarvestPermitApplicationSpeciesAmount entity,
                           final DerogationPermitApplicationForbiddenMethodsSpeciesDTO dto) {
            entity.setForbiddenMethodJustification(dto.getJustification());
            entity.setForbiddenMethodsUsed(dto.isForbiddenMethodsUsed());
        }

        @Override
        public int getSpeciesCode(final DerogationPermitApplicationForbiddenMethodsSpeciesDTO dto) {
            return dto.getGameSpeciesCode();
        }
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }
}
