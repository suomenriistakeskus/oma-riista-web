package fi.riista.feature.permit.application.carnivore;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class CarnivorePermitApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public CarnivorePermitApplication findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public CarnivorePermitApplication findForUpdate(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final CarnivorePermitApplication existing =
                carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkState(existing != null, "Carnivore application not found.");
        return existing;
    }
}
