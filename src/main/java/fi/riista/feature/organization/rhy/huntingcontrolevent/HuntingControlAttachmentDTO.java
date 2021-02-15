package fi.riista.feature.organization.rhy.huntingcontrolevent;

import org.hibernate.validator.constraints.SafeHtml;

public class HuntingControlAttachmentDTO {

    private long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String fileName;

    public HuntingControlAttachmentDTO() {}

    public static HuntingControlAttachmentDTO create(final HuntingControlAttachment attachment) {
        final HuntingControlAttachmentDTO dto = new HuntingControlAttachmentDTO();

        dto.setId(attachment.getId());
        dto.setFileName(attachment.getAttachmentMetadata().getOriginalFilename());

        return dto;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }
}
