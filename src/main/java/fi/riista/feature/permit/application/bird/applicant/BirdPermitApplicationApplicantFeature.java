package fi.riista.feature.permit.application.bird.applicant;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.PermitHolderDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Component
public class BirdPermitApplicationApplicantFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    // PERMIT HOLDER

    @Transactional(readOnly = true)
    public PermitHolderDTO getPermitHolderInfo(final long applicationId) {
        return PermitHolderDTO.createFrom(
                harvestPermitApplicationAuthorizationService.readApplication(applicationId).getPermitHolder());
    }

    @Transactional
    public void updatePermitHolder(final long applicationId, final @NotNull PermitHolderDTO permitHolder) {
        HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        application.setPermitHolder(permitHolder.toEntity());
    }
}
