package fi.riista.feature.permit.application.carnivore;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitSpecies;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class CarnivorePermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private CarnivorePermitApplicationService carnivorePermitApplicationService;

    // READ

    @Transactional(readOnly = true)
    public CarnivorePermitApplicationSummaryDTO readDetails(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        final CarnivorePermitApplication carnivorePermitApplication = carnivorePermitApplicationService.findForRead(applicationId);
        CarnivorePermitSpecies.assertCategory(application.getHarvestPermitCategory());

        return CarnivorePermitApplicationSummaryDTO.from(application, carnivorePermitApplication);
    }
}
