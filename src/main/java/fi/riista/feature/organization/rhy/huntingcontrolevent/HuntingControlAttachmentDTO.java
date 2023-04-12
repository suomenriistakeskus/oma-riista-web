package fi.riista.feature.organization.rhy.huntingcontrolevent;

import org.hibernate.validator.constraints.SafeHtml;

public class HuntingControlAttachmentDTO {

    private long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String fileName;

    private boolean isImage;

    public HuntingControlAttachmentDTO() {}

    public static HuntingControlAttachmentDTO create(final HuntingControlAttachment attachment) {
        final HuntingControlAttachmentDTO dto = new HuntingControlAttachmentDTO();

        dto.setId(attachment.getId());
        dto.setFileName(attachment.getAttachmentMetadata().getOriginalFilename());

        final String contentType = attachment.getAttachmentMetadata().getContentType().toLowerCase();
        dto.setImage(contentType.startsWith("image") || contentType.startsWith("jpeg"));

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

    public boolean isImage() {
        return isImage;
    }

    public void setImage(final boolean image) {
        isImage = image;
    }
}
