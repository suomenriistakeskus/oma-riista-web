package fi.riista.feature.permit.application.deportation.forbidden;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationService;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeportationForbiddenMethodsFeature {

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private DeportationPermitApplicationService deportationPermitApplicationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public DerogationPermitApplicationForbiddenMethodsDTO getForbiddenMethods(final long applicationId) {
        final DeportationPermitApplication deportationPermitApplication =
                deportationPermitApplicationService.findForRead(applicationId);

        final List<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> forbiddenMethodsList = deportationPermitApplication
                .getHarvestPermitApplication().getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new)
                .collect(Collectors.toList());

        return DerogationPermitApplicationForbiddenMethodsDTO.createFrom(deportationPermitApplication.getForbiddenMethods(), forbiddenMethodsList);
    }

    @Transactional
    public void updateForbiddenMethods(final long applicationId,
                                       final @NotNull DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
        final DeportationPermitApplication deportationPermitApplication =
                deportationPermitApplicationService.findOrCreateForUpdate(applicationId);
        final HarvestPermitApplication application = deportationPermitApplication.getHarvestPermitApplication();
        deportationPermitApplication.setForbiddenMethods(forbiddenMethods.toEntity());
        deportationPermitApplicationRepository.save(deportationPermitApplication);

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
