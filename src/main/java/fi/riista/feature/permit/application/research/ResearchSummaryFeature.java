package fi.riista.feature.permit.application.research;

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
public class ResearchSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    // READ

    @Transactional(readOnly = true)
    public ResearchSummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.RESEARCH,
                "Only research application is supported");

        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);

        final DerogationPermitApplicationReasonsDTO reasonsDTO =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        return ResearchSummaryDTO.create(application, researchPermitApplication, reasonsDTO);
    }
}
