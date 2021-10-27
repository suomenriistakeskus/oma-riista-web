package fi.riista.feature.permit.application.weapontransportation.justification;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WeaponTransportationJustificationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

    @Resource
    private TransportedWeaponRepository transportedWeaponRepository;

    @Resource
    private WeaponTransportationVehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public JustificationDTO getJustification(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<TransportedWeaponDTO> transportedWeaponDTOs =
                transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication)
                .stream()
                .map(TransportedWeaponDTO::new)
                .collect(Collectors.toList());
        final List<WeaponTransportationVehicleDTO> vehicleDTOs =
                vehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication)
                .stream()
                .map(WeaponTransportationVehicleDTO::new)
                .collect(Collectors.toList());

        return JustificationDTO.create(transportApplication, transportedWeaponDTOs, vehicleDTOs);
    }

    @Transactional
    public void updateJustification(final long applicationId, final JustificationDTO justification) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);

        Objects.requireNonNull(transportApplication,
                "Weapon transportation application must be set for justification");

        transportedWeaponRepository.deleteByWeaponTransportationPermitApplication(transportApplication);
        if (!justification.getTransportedWeapons().isEmpty()) {
            final List<TransportedWeapon> transportedWeapons = justification.getTransportedWeapons().stream()
                    .map(info -> new TransportedWeapon(transportApplication, info.getType(), info.getDescription()))
                    .collect(Collectors.toList());
            transportedWeaponRepository.saveAll(transportedWeapons);
        }

        vehicleRepository.deleteByWeaponTransportationPermitApplication(transportApplication);
        if (!justification.getVehicles().isEmpty()) {
            final List<WeaponTransportationVehicle> vehicles = justification.getVehicles().stream()
                    .map(vehicle -> new WeaponTransportationVehicle(transportApplication, vehicle.getType(), vehicle.getDescription()))
                    .collect(Collectors.toList());
            vehicleRepository.saveAll(vehicles);
        }

        transportApplication.setJustification(justification.getJustification());
    }

}
