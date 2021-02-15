package fi.riista.feature.permit.application.research;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ResearchPermitApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResearchPermitApplication findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return researchPermitApplicationRepository.findByHarvestPermitApplication(application);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResearchPermitApplication findOrCreateForUpdate(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final ResearchPermitApplication existing =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);

        if (existing != null) {
            return existing;
        }

        final ResearchPermitApplication created = new ResearchPermitApplication(application);
        return created;
    }
}
