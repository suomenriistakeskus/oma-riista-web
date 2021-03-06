package fi.riista.feature.permit.application.derogation.attachments;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentRepository;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DerogationPermitApplicationAttachmentFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationAttachmentRepository harvestPermitApplicationAttachmentRepository;

    @Transactional(readOnly = true)
    public List<DerogationPermitApplicationAttachmentDTO> listAttachments(
            final long applicationId, final @Nullable HarvestPermitApplicationAttachment.Type typeFilter) {
        final HarvestPermitApplication application = harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        return application.getAttachments().stream()
                .filter(a -> typeFilter == null || a.getAttachmentType() == typeFilter)
                .map(DerogationPermitApplicationAttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAttachmentDescriptions(final long applicationId,
                                             final List<DerogationPermitApplicationAttachmentDTO> dtoList) {
        final HarvestPermitApplication application = harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationAttachment> attachmentList =
                harvestPermitApplicationAttachmentRepository.findByHarvestPermitApplication(application);

        final Map<Long, HarvestPermitApplicationAttachment> attachmentIndex = F.indexById(attachmentList);

        for (final DerogationPermitApplicationAttachmentDTO dto : dtoList) {
            final HarvestPermitApplicationAttachment attachment = attachmentIndex.get(dto.getId());

            if (attachment == null) {
                throw new IllegalArgumentException("Invalid attachmentId " + dto.getId());
            }

            attachment.setAdditionalInfo(dto.getAdditionalInfo());
        }
    }
}
