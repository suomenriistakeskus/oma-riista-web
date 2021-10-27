package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.util.DateUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class OtherwiseDeceasedAttachmentFeature {

    @Resource
    private OtherwiseDeceasedAttachmentService attachmentService;

    @Resource
    private OtherwiseDeceasedAttachmentRepository attachmentRepository;

    @Resource
    private OtherwiseDeceasedChangeRepository changeRepository;

    @Resource
    private ActiveUserService activeUserService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional(rollbackFor = IOException.class)
    public void deleteAttachment(final long id) {
        final OtherwiseDeceasedAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceasedAttachment not found, id:" + id));
        addAttachmentDeletionChange(attachment);
        attachmentService.delete(attachment);
    }

    private void addAttachmentDeletionChange(final OtherwiseDeceasedAttachment attachment) {
        final OtherwiseDeceasedChange change = new OtherwiseDeceasedChange(
                attachment.getOtherwiseDeceased(),
                DateUtil.now(),
                activeUserService.requireActiveUserId(),
                OtherwiseDeceasedChange.ChangeType.DELETE_ATTACHMENT,
                attachment.getAttachmentMetadata().getOriginalFilename());
        changeRepository.save(change);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long id) throws IOException {
        final OtherwiseDeceasedAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceasedAttachment not found, id:" + id));
        return attachmentService.getAttachment(attachment);
    }

}
