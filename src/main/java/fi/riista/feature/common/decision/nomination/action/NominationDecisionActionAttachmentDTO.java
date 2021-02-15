package fi.riista.feature.common.decision.nomination.action;

import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public class NominationDecisionActionAttachmentDTO {
    private final long id;
    private final String filename;
    private final LocalDateTime creationTime;

    public NominationDecisionActionAttachmentDTO(final NominationDecisionActionAttachment attachment,
                                                 final PersistentFileMetadata metadata) {
        this.id = requireNonNull(attachment).getId();
        this.filename = requireNonNull(metadata).getOriginalFilename();
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
