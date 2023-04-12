package fi.riista.feature.huntingclub.deercensus.attachment;

public class DeerCensusAttachmentDTO {
    private final Long id;
    private final DeerCensusAttachment.Type type;
    private final String name;

    public DeerCensusAttachmentDTO(final DeerCensusAttachment attachment) {
        this.id = attachment.getId();
        this.type = attachment.getAttachmentType();
        this.name = attachment.getAttachmentMetadata().getOriginalFilename();
    }

    public Long getId() {
        return id;
    }

    public DeerCensusAttachment.Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
