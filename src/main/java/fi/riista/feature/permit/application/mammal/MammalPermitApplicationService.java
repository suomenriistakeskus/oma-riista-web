package fi.riista.feature.permit.application.mammal;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class MammalPermitApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MammalPermitApplication findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MammalPermitApplication findOrCreateForUpdate(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final MammalPermitApplication existing =
                mammalPermitApplicationRepository.findByHarvestPermitApplication(application);

        if (existing != null) {
            return existing;
        }

        final MammalPermitApplication created = new MammalPermitApplication();
        created.setHarvestPermitApplication(application);
        return created;
    }
}
