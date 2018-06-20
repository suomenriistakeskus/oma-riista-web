package fi.riista.feature.permit.decision.attachment;

import org.springframework.web.multipart.MultipartFile;

public class PermitDecisionAttachmentUploadDTO {
    private long decisionId;
    private MultipartFile file;
    private String description;

    public long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(final long decisionId) {
        this.decisionId = decisionId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(final MultipartFile file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
