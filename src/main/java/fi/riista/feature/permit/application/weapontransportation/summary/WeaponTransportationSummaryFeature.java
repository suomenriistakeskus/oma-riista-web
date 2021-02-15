package fi.riista.feature.permit.application.weapontransportation.summary;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeapon;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicle;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Service
public class WeaponTransportationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

    @Resource
    private TransportedWeaponRepository transportedWeaponRepository;

    @Resource
    private WeaponTransportationVehicleRepository vehicleRepository;

    // READ

    @Transactional(readOnly = true)
    public SummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application = harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.WEAPON_TRANSPORTATION,
                "Only weapon transportation application is supported");

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<TransportedWeapon> transportedWeapons =
                transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
        final List<WeaponTransportationVehicle> vehicles =
                vehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
        return SummaryDTO.create(application, transportApplication, transportedWeapons, vehicles);
    }
}
