package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.hibernate.validator.constraints.SafeHtml;

public class OtherwiseDeceasedAttachmentDTO {

    // Factories

    public static OtherwiseDeceasedAttachmentDTO create(final OtherwiseDeceasedAttachment attachment) {
        return create(attachment, attachment.getAttachmentMetadata());
    }

    public static OtherwiseDeceasedAttachmentDTO create(final OtherwiseDeceasedAttachment attachment,
                                                        final PersistentFileMetadata metadata) {
        final OtherwiseDeceasedAttachmentDTO dto = new OtherwiseDeceasedAttachmentDTO();

        dto.setId(attachment.getId());
        dto.setFilename(metadata.getOriginalFilename());

        return dto;
    }

    // Attributes

    private long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String filename;

    // Accessors

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }
}
