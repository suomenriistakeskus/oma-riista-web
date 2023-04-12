package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.RequireEntityService;
import fi.riista.security.EntityPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
public class HuntingControlEventAttachmentFeature {
    @Resource
    private HuntingControlEventAttachmentService attachmentService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingControlEventChangeService changeService;

    @Transactional(rollbackFor = IOException.class)
    public void deleteAttachment(final long id) {
        final HuntingControlAttachment attachment = requireEntityService.requireHuntingControlAttachment(id, EntityPermission.DELETE);
        changeService.addDeleteAttachment(attachment.getHuntingControlEvent(), attachment.getAttachmentMetadata().getOriginalFilename());
        attachmentService.delete(attachment);
    }

    @Transactional(readOnly = true)
    public List<HuntingControlAttachmentDTO> listAttachments(final long eventId) {
        final HuntingControlEvent event = requireEntityService.requireHuntingControlEvent(eventId, EntityPermission.READ);
        return attachmentService.listAttachments(event);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long attachmentId) throws IOException {
        final HuntingControlAttachment attachment = requireEntityService.requireHuntingControlAttachment(attachmentId, EntityPermission.READ);
        return attachmentService.getAttachment(attachment);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getThumbnail(final long attachmentId) throws IOException {
        final HuntingControlAttachment attachment = requireEntityService.requireHuntingControlAttachment(attachmentId, EntityPermission.READ);
        return attachmentService.getThumbnail(attachment);
    }
}
