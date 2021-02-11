package fi.riista.feature.permit.application.derogation.reasons;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Component
public class DerogationPermitApplicationReasonFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    @Transactional(readOnly = true)
    public DerogationPermitApplicationReasonsDTO getDerogationReasons(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(applicationId,
                        EntityPermission.READ);

        return derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
    }

    @Transactional
    public void updateDerogationReasons(final long applicationId, final DerogationPermitApplicationReasonsDTO dto) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(applicationId,
                        EntityPermission.UPDATE);

        derogationPermitApplicationReasonService.updateDerogationReasons(application, dto);
    }

}
