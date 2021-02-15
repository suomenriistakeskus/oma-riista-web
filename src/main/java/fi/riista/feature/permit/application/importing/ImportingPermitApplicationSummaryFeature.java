package fi.riista.feature.permit.application.importing;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.OTHER;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.PROTECTED_AREA;

@Service
public class ImportingPermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private ImportingPermitApplicationRepository importingPermitApplicationRepository;

    // READ

    @Transactional(readOnly = true)
    public ImportingPermitApplicationSummaryDTO readDetails(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        final ImportingPermitApplication importingPermitApplication =
                importingPermitApplicationRepository.findByHarvestPermitApplication(application);

        final List<HarvestPermitApplicationAttachment> attachments = application.getAttachments();
        final List<HarvestPermitApplicationAttachment> areaAttachments =
                F.filterToList(attachments, a -> a.getAttachmentType() == PROTECTED_AREA);
        final List<HarvestPermitApplicationAttachment> otherAttachments =
                F.filterToList(attachments, a -> a.getAttachmentType() == OTHER);

        return ImportingPermitApplicationSummaryDTO.from(application, importingPermitApplication,
                application.getSpeciesAmounts(), areaAttachments, otherAttachments);
    }
}
