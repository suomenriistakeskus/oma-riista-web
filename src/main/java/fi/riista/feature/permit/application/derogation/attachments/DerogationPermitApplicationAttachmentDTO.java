package fi.riista.feature.permit.application.derogation.attachments;

import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.validation.XssSafe;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class DerogationPermitApplicationAttachmentDTO {

    @NotNull
    private Long id;

    @XssSafe
    private String name;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    public DerogationPermitApplicationAttachmentDTO() {
    }

    public DerogationPermitApplicationAttachmentDTO(final @Nonnull HarvestPermitApplicationAttachment attachment) {
        requireNonNull(attachment);

        this.id = attachment.getId();
        this.name = attachment.getAttachmentMetadata().getOriginalFilename();
        this.additionalInfo = attachment.getAdditionalInfo();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
