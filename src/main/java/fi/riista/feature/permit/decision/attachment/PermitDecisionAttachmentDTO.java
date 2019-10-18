package fi.riista.feature.permit.decision.attachment;

import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionAttachment;

public class PermitDecisionAttachmentDTO {
    private final long id;
    private final Integer orderingNumber;
    private final String description;

    public PermitDecisionAttachmentDTO(final PermitDecisionAttachment attachment) {
        this.id = attachment.getId();
        this.orderingNumber = attachment.getOrderingNumber();
        this.description = attachment.getDescription();
    }

    public PermitDecisionAttachmentDTO(final PermitDecisionRevisionAttachment attachment) {
        this.id = attachment.getDecisionAttachment().getId();
        this.orderingNumber = attachment.getOrderingNumber();
        this.description = attachment.getDecisionAttachment().getDescription();
    }

    public long getId() {
        return id;
    }

    public Integer getOrderingNumber() {
        return orderingNumber;
    }

    public String getDescription() {
        return description;
    }
}
