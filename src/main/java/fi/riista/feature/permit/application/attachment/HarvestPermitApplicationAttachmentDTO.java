package fi.riista.feature.permit.application.attachment;

public class HarvestPermitApplicationAttachmentDTO {
    private final Long id;
    private final HarvestPermitApplicationAttachment.Type type;
    private final String name;

    public HarvestPermitApplicationAttachmentDTO(final HarvestPermitApplicationAttachment attachment) {
        this.id = attachment.getId();
        this.type = attachment.getAttachmentType();
        this.name = attachment.getAttachmentMetadata().getOriginalFilename();
    }

    public Long getId() {
        return id;
    }

    public HarvestPermitApplicationAttachment.Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
