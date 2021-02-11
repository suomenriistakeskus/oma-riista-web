package fi.riista.feature.permit.application.weapontransportation.reason;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class WeaponTransportationReasonFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService applicationAuthorizationService;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationRepository;

    @Transactional(readOnly = true)
    public ReasonDTO getReason(final long applicationId) {
        final HarvestPermitApplication application = applicationAuthorizationService.readApplication(applicationId);

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationRepository.findByHarvestPermitApplication(application);

        return ReasonDTO.create(transportApplication);
    }

    @Transactional
    public void updateReason(final long applicationId, final ReasonDTO reason) {
        final HarvestPermitApplication application = applicationAuthorizationService.updateApplication(applicationId);

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationRepository.findByHarvestPermitApplication(application);

        Objects.requireNonNull(transportApplication,
                "Weapon transportation application must be set for reasons");

        transportApplication.setReasonType(reason.getReasonType());
        transportApplication.setReasonDescription(reason.getReasonDescription());
        transportApplication.setBeginDate(reason.getBeginDate());
        transportApplication.setEndDate(reason.getEndDate());
    }
}
