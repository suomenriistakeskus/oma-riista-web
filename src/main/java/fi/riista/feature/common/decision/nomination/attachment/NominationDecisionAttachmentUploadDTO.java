package fi.riista.feature.common.decision.nomination.attachment;

import org.springframework.web.multipart.MultipartFile;

public class NominationDecisionAttachmentUploadDTO {
    private final long decisionId;
    private final MultipartFile file;
    private final String description;

    public NominationDecisionAttachmentUploadDTO(final long decisionId, final MultipartFile file,
                                                 final String description) {
        this.decisionId = decisionId;
        this.file = file;
        this.description = description;
    }

    public long getDecisionId() {
        return decisionId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public String getDescription() {
        return description;
    }

}
