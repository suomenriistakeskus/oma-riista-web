package fi.riista.feature.permit.application.disability.summary;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentRepository;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfo;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfoRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicle;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Service
public class DisabilityPermitSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private DisabilityPermitApplicationRepository disabilityPermitApplicationRepository;

    @Resource
    private DisabilityPermitVehicleRepository vehicleRepository;

    @Resource
    private DisabilityPermitHuntingTypeInfoRepository huntingTypeInfoRepository;

    @Resource
    private HarvestPermitApplicationAttachmentRepository attachmentRepository;

    // READ

    @Transactional(readOnly = true)
    public DisabilityPermitSummaryDTO readDetails(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application = harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.DISABILITY,
                "Only disability application is supported");

        final DisabilityPermitApplication disabilityPermitApplication =
                disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<DisabilityPermitVehicle> vehicles =
                vehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
        final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                huntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
        final List<HarvestPermitApplicationAttachment> attachments =
                attachmentRepository.findByHarvestPermitApplication(application);

        return DisabilityPermitSummaryDTO.create(application, disabilityPermitApplication, vehicles, huntingTypeInfos,
                application.getContactPerson(), attachments);
    }
}
