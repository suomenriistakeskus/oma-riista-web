package fi.riista.feature.permit.application.research.justification;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class ResearchJustificationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Transactional(readOnly = true)
    public ResearchJustificationDTO getJustification(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);

        return ResearchJustificationDTO.create(researchPermitApplication);
    }

    @Transactional
    public void updateJustification(final long applicationId, final ResearchJustificationDTO justification) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);

        Objects.requireNonNull(researchPermitApplication,
                "Research permit application must be set for justification");

        researchPermitApplication.setJustification(justification.getJustification());
    }
}
