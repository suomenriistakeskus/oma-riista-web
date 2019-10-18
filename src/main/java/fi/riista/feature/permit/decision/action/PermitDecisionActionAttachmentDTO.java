package fi.riista.feature.permit.decision.action;

import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

public class PermitDecisionActionAttachmentDTO {
    private final long id;
    private final String filename;
    private final LocalDateTime creationTime;

    public PermitDecisionActionAttachmentDTO(final PermitDecisionActionAttachment attachment) {
        this.id = attachment.getId();
        this.filename = attachment.getAttachmentMetadata().getOriginalFilename();
        this.creationTime = DateUtil.toLocalDateTimeNullSafe(attachment.getCreationTime());
    }

    public long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }
}
