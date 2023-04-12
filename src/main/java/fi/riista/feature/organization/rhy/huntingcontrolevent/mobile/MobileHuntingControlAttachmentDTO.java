package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlAttachment;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.Optional;

public class MobileHuntingControlAttachmentDTO {

    private long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String fileName;

    private boolean isImage;

    private byte[] thumbnail;

    // Constructors / factories

    static MobileHuntingControlAttachmentDTO create(final HuntingControlAttachment attachment,
                                                    final PersistentFileMetadata metadata) {

        final MobileHuntingControlAttachmentDTO dto = new MobileHuntingControlAttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(metadata.getOriginalFilename());

        final String contentType = metadata.getContentType().toLowerCase();
        dto.setImage(contentType.startsWith("image") || contentType.startsWith("jpeg"));

        return dto;
    }

    // Accessors -->

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

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(final byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }
}
