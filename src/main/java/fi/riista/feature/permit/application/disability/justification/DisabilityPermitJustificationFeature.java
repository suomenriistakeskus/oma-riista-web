package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DisabilityPermitJustificationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private DisabilityPermitApplicationRepository applicationRepository;

    @Resource
    private DisabilityPermitVehicleRepository vehicleRepository;

    @Resource
    private DisabilityPermitHuntingTypeInfoRepository huntingTypeInfoRepository;

    @Transactional(readOnly = true)
    public JustificationDTO getJustification(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final DisabilityPermitApplication disabilityPermitApplication =
                applicationRepository.findByHarvestPermitApplication(application);
        final List<DisabilityPermitVehicleDTO> vehicles =
                vehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication)
                .stream().map(DisabilityPermitVehicleDTO::new).collect(Collectors.toList());
        final List<DisabilityPermitHuntingTypeInfoDTO> huntingTypeInfos =
                huntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication)
                .stream().map(DisabilityPermitHuntingTypeInfoDTO::new).collect(Collectors.toList());

        return JustificationDTO.create(disabilityPermitApplication, vehicles, huntingTypeInfos);
    }

    @Transactional
    public void updateJustification(final long applicationId, final JustificationDTO justification) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final DisabilityPermitApplication disabilityPermitApplication =
                applicationRepository.findByHarvestPermitApplication(application);

        Objects.requireNonNull(disabilityPermitApplication,
                "Disability permit application must be set for justification");

        disabilityPermitApplication.setJustification(justification.getJustification());

        vehicleRepository.deleteByDisabilityPermitApplication(disabilityPermitApplication);
        if(!justification.getVehicles().isEmpty()) {
            final List<DisabilityPermitVehicle> vehicles = justification.getVehicles().stream()
                    .map(vehicle -> new DisabilityPermitVehicle(disabilityPermitApplication, vehicle.getType(),
                            vehicle.getDescription(), vehicle.getJustification()))
                    .collect(Collectors.toList());
            vehicleRepository.saveAll(vehicles);
        }

        huntingTypeInfoRepository.deleteByDisabilityPermitApplication(disabilityPermitApplication);
        if(!justification.getHuntingTypeInfos().isEmpty()) {
            final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos = justification.getHuntingTypeInfos().stream()
                    .map(huntingTypeInfo -> new DisabilityPermitHuntingTypeInfo(disabilityPermitApplication,
                            huntingTypeInfo.getHuntingType(), huntingTypeInfo.getHuntingTypeDescription()))
                    .collect(Collectors.toList());
            huntingTypeInfoRepository.saveAll(huntingTypeInfos);
        }
    }
}
