package fi.riista.feature.permit.application.lawsectionten;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Component
public class LawSectionTenPermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    // READ

    @Transactional(readOnly = true)
    public LawSectionTenPermitApplicationSummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.LAW_SECTION_TEN,
                "Only law section 10 application is supported");

        final LawSectionTenPermitApplication lawSectionTenPermitApplication =
                lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
        return LawSectionTenPermitApplicationSummaryDTO.create(application,
                lawSectionTenPermitApplication);
    }
}
