package fi.riista.feature.harvestpermit.attachment;

import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;

public class HarvestPermitAttachmentDTO {
    private final long id;
    private final Integer orderingNumber;
    private final String description;

    public HarvestPermitAttachmentDTO(final PermitDecisionAttachment attachment) {
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
