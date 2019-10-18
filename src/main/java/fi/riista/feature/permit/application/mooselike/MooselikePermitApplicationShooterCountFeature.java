package fi.riista.feature.permit.application.mooselike;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class MooselikePermitApplicationShooterCountFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Transactional(readOnly = true)
    public MooselikePermitApplicationShooterCountDTO getShooterCounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        return MooselikePermitApplicationShooterCountDTO.create(application);
    }

    @Transactional
    public void updateShooterCounts(final long applicationId,
                                    final MooselikePermitApplicationShooterCountDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        application.setShooterOnlyClub(dto.getShooterOnlyClub());
        application.setShooterOtherClubActive(dto.getShooterOtherClubActive());
        application.setShooterOtherClubPassive(dto.getShooterOtherClubPassive());
    }

}
