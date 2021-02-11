package fi.riista.feature.permit.application.gamemanagement.forbidden;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.gamemanagement.GameManagementApplicationService;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameManagementForbiddenMethodsFeature {

    @Resource
    private GameManagementApplicationService gameManagementApplicationService;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public DerogationPermitApplicationForbiddenMethodsDTO getMethods(final long applicationId) {
        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementApplicationService.findForRead(applicationId);

        final List<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> justificationList = gameManagementPermitApplication
                .getHarvestPermitApplication().getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new)
                .collect(Collectors.toList());

        return DerogationPermitApplicationForbiddenMethodsDTO.createFrom(gameManagementPermitApplication.getForbiddenMethods(), justificationList);
    }

    @Transactional
    public void updateMethods(final long applicationId,
                              final @NotNull DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementApplicationService.findOrCreateForUpdate(applicationId);
        final HarvestPermitApplication application = gameManagementPermitApplication.getHarvestPermitApplication();
        gameManagementPermitApplication.setForbiddenMethods(forbiddenMethods.toEntity());
        gameManagementPermitApplicationRepository.save(gameManagementPermitApplication);

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
