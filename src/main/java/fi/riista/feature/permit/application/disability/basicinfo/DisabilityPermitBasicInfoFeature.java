package fi.riista.feature.permit.application.disability.basicinfo;

import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Service
public class DisabilityPermitBasicInfoFeature {

    private final static String ROVANIEMI_OFFICIAL_CODE = "212";

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private DisabilityPermitApplicationRepository disabilityPermitApplicationRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Transactional(readOnly = true)
    public BasicInfoDTO getBasicInfo(final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        final DisabilityPermitApplication disabilityPermitApplication =
                disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);

        return BasicInfoDTO.create(disabilityPermitApplication);
    }

    @Transactional
    public void updateBasicInfo(final long applicationId, final BasicInfoDTO basicInfo) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);
        final DisabilityPermitApplication disabilityPermitApplication =
                disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);

        requireNonNull(disabilityPermitApplication, "Disability permit application must be set for basic info");

        // Disability permit applications are handled in Rovaniemi
        if (application.getRhy() == null) {
            application.setRhy(rhyRepository.findByOfficialCode(ROVANIEMI_OFFICIAL_CODE));
        }

        disabilityPermitApplication.setUseMotorVehicle(basicInfo.getUseMotorVehicle());
        disabilityPermitApplication.setUseVehicleForWeaponTransport(basicInfo.getUseVehicleForWeaponTransport());

        disabilityPermitApplication.setBeginDate(basicInfo.getBeginDate());
        disabilityPermitApplication.setEndDate(basicInfo.getEndDate());
    }

}
