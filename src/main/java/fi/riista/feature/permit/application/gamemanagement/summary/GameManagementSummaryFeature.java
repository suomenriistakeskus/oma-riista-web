package fi.riista.feature.permit.application.gamemanagement.summary;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GameManagementSummaryFeature {
    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository speciesAmountRepository;

    // READ

    @Transactional(readOnly = true)
    public GameManagementSummaryDTO readDetails(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.GAME_MANAGEMENT,
                "Only game management application is supported");

        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts =
                speciesAmountRepository.findByHarvestPermitApplication(application);

        return GameManagementSummaryDTO.create(application, gameManagementPermitApplication, speciesAmounts);
    }
}
