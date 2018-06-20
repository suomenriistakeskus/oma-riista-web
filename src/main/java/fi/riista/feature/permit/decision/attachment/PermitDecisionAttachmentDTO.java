package fi.riista.feature.permit.decision.attachment;

public class PermitDecisionAttachmentDTO {
    private final long id;
    private final Integer orderingNumber;
    private final String description;

    public PermitDecisionAttachmentDTO(final PermitDecisionAttachment attachment) {
        this.id = attachment.getId();
        this.orderingNumber = attachment.getOrderingNumber();
        this.description = attachment.getDescription();
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
