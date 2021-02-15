package fi.riista.feature.permit.application.weapontransportation.applicant;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.PermitHolderDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Service
public class WeaponTransportationApplicantFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService applicationAuthorizationService;

    // PERMIT HOLDER

    @Transactional(readOnly = true)
    public PermitHolderDTO getPermitHolderInfo(final long applicationId) {
        return PermitHolderDTO.createFrom(applicationAuthorizationService.readApplication(applicationId).getPermitHolder());
    }

    @Transactional
    public void updatePermitHolder(final long applicationId, final @NotNull PermitHolderDTO permitHolder) {
        final HarvestPermitApplication application = applicationAuthorizationService.updateApplication(applicationId);

        application.setPermitHolder(permitHolder.toEntity());
    }

}
