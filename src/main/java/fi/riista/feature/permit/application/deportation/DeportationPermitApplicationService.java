package fi.riista.feature.permit.application.deportation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class DeportationPermitApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public DeportationPermitApplication findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public DeportationPermitApplication findOrCreateForUpdate(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final DeportationPermitApplication existing =
                deportationPermitApplicationRepository.findByHarvestPermitApplication(application);

        if (existing != null) {
            return existing;
        }

        final DeportationPermitApplication created = new DeportationPermitApplication(application);
        return created;
    }
}
