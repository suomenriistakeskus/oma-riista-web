package fi.riista.feature.permit.application;


import fi.riista.feature.RequireEntityService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HarvestPermitApplicationAuthorizationService {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitApplicationLockedCondition harvestPermitApplicationLockedCondition;

    public HarvestPermitApplication readApplication(final long applicationId) {
        return requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
    }

    public HarvestPermitApplication updateApplication(final long applicationId) {
        final HarvestPermitApplication result = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);
        harvestPermitApplicationLockedCondition.assertCanUpdate(result);
        return result;
    }

    public HarvestPermitApplication amendApplication(final long applicationId) {
        return requireEntityService.requireHarvestPermitApplication(
                applicationId, HarvestPermitApplicationAuthorization.Permission.AMEND);
    }
}
