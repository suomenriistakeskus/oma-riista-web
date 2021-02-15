package fi.riista.feature.permit.application.nestremoval;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Service
public class NestRemovalPermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    // READ

    @Transactional(readOnly = true)
    public NestRemovalPermitApplicationSummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.NEST_REMOVAL,
                "Only nest removal application is supported");

        final NestRemovalPermitApplication nestRemovalPermitApplication =
                nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);

        final DerogationPermitApplicationReasonsDTO reasonsDTO =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        return NestRemovalPermitApplicationSummaryDTO.create(application, nestRemovalPermitApplication, reasonsDTO);
    }
}
