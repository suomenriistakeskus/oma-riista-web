package fi.riista.feature.permit.application.research.forbidden;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResearchForbiddenMethodsFeature {

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private ResearchPermitApplicationService researchPermitApplicationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public DerogationPermitApplicationForbiddenMethodsDTO getForbiddenMethods(final long applicationId) {
        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationService.findForRead(applicationId);

        final List<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> forbiddenMethodsList = researchPermitApplication
                .getHarvestPermitApplication().getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new)
                .collect(Collectors.toList());

        return DerogationPermitApplicationForbiddenMethodsDTO.createFrom(researchPermitApplication.getForbiddenMethods(), forbiddenMethodsList);
    }

    @Transactional
    public void updateForbiddenMethods(final long applicationId,
                                       final @NotNull DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationService.findOrCreateForUpdate(applicationId);
        final HarvestPermitApplication application = researchPermitApplication.getHarvestPermitApplication();
        researchPermitApplication.setForbiddenMethods(forbiddenMethods.toEntity());
        researchPermitApplicationRepository.save(researchPermitApplication);

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
