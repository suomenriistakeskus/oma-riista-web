package fi.riista.feature.permit.application.gamemanagement.justification;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class GameManagementJustificationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Transactional(readOnly = true)
    public GameManagementJustificationDTO getJustification(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);

        return GameManagementJustificationDTO.create(gameManagementPermitApplication);
    }

    @Transactional
    public void updateJustification(final long applicationId, final GameManagementJustificationDTO justification) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);

        Objects.requireNonNull(gameManagementPermitApplication,
                "Game management permit application must be set for justification");

        gameManagementPermitApplication.setJustification(justification.getJustification());
    }

}
