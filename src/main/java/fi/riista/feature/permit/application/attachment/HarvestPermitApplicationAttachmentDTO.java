package fi.riista.feature.permit.application.attachment;

public class HarvestPermitApplicationAttachmentDTO {
    private final Long id;
    private final HarvestPermitApplicationAttachment.Type type;
    private final boolean remote;
    private final String name;

    public HarvestPermitApplicationAttachmentDTO(final HarvestPermitApplicationAttachment attachment) {
        this.id = attachment.getId();
        this.type = attachment.getAttachmentType();
        this.remote = attachment.getUrl() != null;
        this.name = attachment.getName();
    }

    public Long getId() {
        return id;
    }

    public HarvestPermitApplicationAttachment.Type getType() {
        return type;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getName() {
        return name;
    }
}
