package fi.riista.feature.permit.application.gamemanagement;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class GameManagementApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameManagementPermitApplication findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameManagementPermitApplication findOrCreateForUpdate(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final GameManagementPermitApplication existing =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);

        if (existing != null) {
            return existing;
        }

        final GameManagementPermitApplication created = new GameManagementPermitApplication(application);
        return created;
    }
}
