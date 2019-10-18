package fi.riista.feature.permit.application.partner;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ModifyPermitApplicationPartnersFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitAreaPartnerService harvestPermitAreaPartnerService;

    @Transactional
    public void addPartner(final AddPermitApplicationPartnerDTO dto) {
        final HarvestPermitArea area = requirePermitArea(dto.getApplicationId());
        harvestPermitAreaPartnerService.addPartner(area, dto.getExternalId());
    }


    @Transactional
    public void removePartner(final long applicationId, final long partnerId) {
        harvestPermitAreaPartnerService.removePartner(requirePermitArea(applicationId), partnerId);
    }

    @Transactional
    public void refreshPartner(final long applicationId, final long partnerId) {
        harvestPermitAreaPartnerService.refreshPartner(requirePermitArea(applicationId), partnerId);
    }

    private HarvestPermitArea requirePermitArea(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);
        application.assertHasPermitArea();
        return application.getArea();
    }
}
