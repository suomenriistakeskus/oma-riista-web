package fi.riista.feature.common.decision.nomination.attachment;

public class NominationDecisionAttachmentDTO {
    private final long id;
    private final Integer orderingNumber;
    private final String description;

    public NominationDecisionAttachmentDTO(final NominationDecisionAttachment attachment) {
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
