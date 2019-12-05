package fi.riista.feature.permit.application.mammal;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Component
public class MammalPermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    // READ

    @Transactional(readOnly = true)
    public MammalPermitApplicationSummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.MAMMAL,
                "Only mammal application is supported");

        final MammalPermitApplication mammalPermitApplication =
                mammalPermitApplicationRepository.findByHarvestPermitApplication(application);

        final DerogationPermitApplicationReasonsDTO reasonsDTO =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        return MammalPermitApplicationSummaryDTO.create(application, mammalPermitApplication, reasonsDTO);
    }
}
