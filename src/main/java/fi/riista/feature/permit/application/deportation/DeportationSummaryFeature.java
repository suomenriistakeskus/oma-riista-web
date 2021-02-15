package fi.riista.feature.permit.application.deportation;

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
public class DeportationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    // READ

    @Transactional(readOnly = true)
    public DeportationSummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.DEPORTATION,
                "Only deportation application is supported");

        final DeportationPermitApplication deportationPermitApplication =
                deportationPermitApplicationRepository.findByHarvestPermitApplication(application);

        final DerogationPermitApplicationReasonsDTO reasonsDTO =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        return DeportationSummaryDTO.create(application, deportationPermitApplication, reasonsDTO);
    }
}
