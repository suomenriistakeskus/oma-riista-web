package fi.riista.feature.permit.application.bird;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class BirdPermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    // READ

    @Transactional(readOnly = true)
    public BirdPermitApplicationSummaryDTO readDetails(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.BIRD,
                "Only bird application is supported");

        final BirdPermitApplication birdApplication =
                birdPermitApplicationRepository.findByHarvestPermitApplication(application);

        return BirdPermitApplicationSummaryDTO.create(application, birdApplication);
    }
}
